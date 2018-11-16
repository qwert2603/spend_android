package com.qwert2603.spenddemo.records_list_mvvm

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.entity.RecordsListItem
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class RecordsListPresenter @Inject constructor(
        private val recordsListInteractor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {

    override val initialState = RecordsListViewState(
            records = emptyList(),
            diff = FastDiffUtils.FastDiffResult.EMPTY,
            showInfo = recordsListInteractor.showInfo,
            longSumPeriodDays = recordsListInteractor.longSumPeriodDays,
            shortSumPeriodMinutes = recordsListInteractor.shortSumPeriodMinutes,
            sumsInfo = SumsInfo.EMPTY,
            syncingRecordsUuids = emptySet()
    )

    sealed class ShowInfoChange {
        data class Spends(val show: Boolean) : ShowInfoChange()
        data class Profits(val show: Boolean) : ShowInfoChange()
        data class Sums(val show: Boolean) : ShowInfoChange()
        data class ChangeKinds(val show: Boolean) : ShowInfoChange()
        data class Times(val show: Boolean) : ShowInfoChange()
    }

    private val showInfoChanges: Observable<ShowInfo> = Observable
            .merge(listOf(
                    intent { it.showSpendsChanges() }.map { ShowInfoChange.Spends(it) },
                    intent { it.showProfitsChanges() }.map { ShowInfoChange.Profits(it) },
                    intent { it.showSumsChanges() }.map { ShowInfoChange.Sums(it) },
                    intent { it.showChangeKindsChanges() }.map { ShowInfoChange.ChangeKinds(it) },
                    intent { it.showTimesChanges() }.map { ShowInfoChange.Times(it) }
            ))
            .scan(initialState.showInfo) { info: ShowInfo, ch: ShowInfoChange ->
                return@scan when (ch) {
                    is ShowInfoChange.Spends -> info.copy(showSpends = ch.show)
                    is ShowInfoChange.Profits -> info.copy(showProfits = ch.show)
                    is ShowInfoChange.Sums -> info.copy(showSums = ch.show)
                    is ShowInfoChange.ChangeKinds -> info.copy(showChangeKinds = ch.show)
                    is ShowInfoChange.Times -> info.copy(showTimes = ch.show)
                }
            }
            .shareAfterViewSubscribed()

    private val longSumPeriodDaysChanges = intent { it.longSumPeriodDaysSelected() }
            .startWith(initialState.longSumPeriodDays)
            .shareAfterViewSubscribed()

    private val shortSumPeriodMinutesChanges = intent { it.shortSumPeriodMinutesSelected() }
            .startWith(initialState.shortSumPeriodMinutes)
            .shareAfterViewSubscribed()

    override val partialChanges: Observable<PartialChange> = Observable.merge(listOf(
            showInfoChanges
                    .skip(1) // skip initial.
                    .map { RecordsListPartialChange.ShowInfoChanged(it) },
            showInfoChanges
                    .switchMap { showInfo ->
                        recordsListInteractor.getRecordsList()
                                .map { it.toRecordItemsList(showInfo) }
                    }
                    .startWith(initialState.records)
                    .buffer(2, 1)
                    .map { (prev, current) ->
                        val diffResult = FastDiffUtils.fastCalculateDiff(
                                oldList = prev,
                                newList = current,
                                id = RecordsListItem::idInList,
                                compareOrder = RecordsListItem.COMPARE_ORDER,
                                isEqual = RecordsListItem::equals,
                                possiblyMovedItemIds = emptyList()//todo
                        )
                        RecordsListPartialChange.RecordsListChanged(current, diffResult)
                    },
            longSumPeriodDaysChanges
                    .skip(1) // skip initial.
                    .map { RecordsListPartialChange.LongSumPeriodDaysChanged(it) },
            shortSumPeriodMinutesChanges
                    .skip(1) // skip initial.
                    .map { RecordsListPartialChange.ShortSumPeriodMinutesChanged(it) },
            Observable
                    .combineLatest(
                            longSumPeriodDaysChanges,
                            shortSumPeriodMinutesChanges,
                            showInfoChanges,
                            RxUtils.minuteChanges(),
                            makeQuad()
                    )
                    .switchMap { (longSumPeriodDays, shortSumPeriodMinutes, showInfo) ->
                        val longSumChanges = Observable
                                .combineLatest(
                                        if (showInfo.showSpendSum()) {
                                            recordsListInteractor.getSumLastDays(Const.RECORD_TYPE_ID_SPEND, longSumPeriodDays)
                                        } else {
                                            Observable.just(0L)
                                        },
                                        if (showInfo.showProfitSum()) {
                                            recordsListInteractor.getSumLastDays(Const.RECORD_TYPE_ID_PROFIT, longSumPeriodDays)
                                        } else {
                                            Observable.just(0L)
                                        },
                                        BiFunction { s: Long, p: Long -> p - s }
                                )
                        val shortSumChanges = Observable
                                .combineLatest(
                                        if (showInfo.showSpendSum()) {
                                            recordsListInteractor.getSumLastMinutes(Const.RECORD_TYPE_ID_SPEND, shortSumPeriodMinutes)
                                        } else {
                                            Observable.just(0L)
                                        },
                                        if (showInfo.showProfitSum()) {
                                            recordsListInteractor.getSumLastMinutes(Const.RECORD_TYPE_ID_PROFIT, shortSumPeriodMinutes)
                                        } else {
                                            Observable.just(0L)
                                        },
                                        BiFunction { s: Long, p: Long -> p - s }
                                )
                        val changesCountChanges = recordsListInteractor
                                .getLocalChangesCount(listOfNotNull(
                                        Const.RECORD_TYPE_ID_SPEND.takeIf { showInfo.showSpendSum() },
                                        Const.RECORD_TYPE_ID_PROFIT.takeIf { showInfo.showProfitSum() }
                                ))

                        Observable
                                .combineLatest(
                                        if (longSumPeriodDays > 0) longSumChanges.map { it.wrap() } else Observable.just(Wrapper(null)),
                                        if (shortSumPeriodMinutes > 0) shortSumChanges.map { it.wrap() } else Observable.just(Wrapper(null)),
                                        if (showInfo.showChangeKinds) changesCountChanges.map { it.wrap() } else Observable.just(Wrapper(null)),
                                        Function3 { longSum: Wrapper<out Long>, shortSum: Wrapper<out Long>, changesCount: Wrapper<out Int> ->
                                            SumsInfo(longSum.t, shortSum.t, changesCount.t)
                                        }
                                )
                                .map { RecordsListPartialChange.SumsInfoChanged(it) }
                    },
            recordsListInteractor.getSyncingRecordsUuids()
                    .map { RecordsListPartialChange.SyncingRecordsUuidsChanged(it) }
    ))

    override fun stateReducer(vs: RecordsListViewState, change: PartialChange): RecordsListViewState {
        if (change !is RecordsListPartialChange) throw Exception()
        return when (change) {
            is RecordsListPartialChange.RecordsListChanged -> vs.copy(records = change.list, diff = change.diff)
            is RecordsListPartialChange.ShowInfoChanged -> vs.copy(showInfo = change.showInfo)
            is RecordsListPartialChange.SumsInfoChanged -> vs.copy(sumsInfo = change.sumsInfo)
            is RecordsListPartialChange.LongSumPeriodDaysChanged -> vs.copy(longSumPeriodDays = change.days)
            is RecordsListPartialChange.ShortSumPeriodMinutesChanged -> vs.copy(shortSumPeriodMinutes = change.minutes)
            is RecordsListPartialChange.SyncingRecordsUuidsChanged -> vs.copy(syncingRecordsUuids = change.uuids)
        }
    }

    override fun bindIntents() {
        intent { it.createProfitClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToCreateRecord(Const.RECORD_TYPE_ID_PROFIT)) }
                .subscribeToView()

        intent { it.chooseLongSumPeriodClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToChooseLongSumPeriod(it.longSumPeriodDays)) }
                .subscribeToView()

        intent { it.chooseShortSumPeriodClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToChooseShortSumPeriod(it.shortSumPeriodMinutes)) }
                .subscribeToView()

        intent { it.recordClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToEditRecord(it.uuid)) }
                .subscribeToView()

        intent { it.recordLongClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToDeleteRecord(it.uuid)) }
                .subscribeToView()

        intent { it.makeDumpClicks() }
                .flatMapSingle {
                    recordsListInteractor.getDumpText()
                            .doOnSubscribe { viewActions.onNext(RecordsListViewAction.ShowDumpIsCreating) }
                            .doOnSuccess { viewActions.onNext(RecordsListViewAction.SendDump(it)) }
                            .onErrorReturnItem("dump error!")
                }
                .subscribeToView()

        intent { it.addStubRecordsClicks() }
                .doOnNext {
                    val stubSpendKinds = listOf("трамвай", "столовая", "шоколадка", "автобус")
                    val stubProfitKinds = listOf("стипендия", "зарплата", "аванс", "доход")
                    recordsListInteractor.addRecords(
                            (1..200).map {
                                RecordDraft(
                                        uuid = UUID.randomUUID().toString(),
                                        recordTypeId = Const.RECORD_TYPE_ID_SPEND,
                                        date = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -Random.nextInt(5000)) }.toDateInt(),
                                        time = Calendar.getInstance().also { it.add(Calendar.MINUTE, -Random.nextInt(1440)) }.toTimeInt().takeIf { Random.nextBoolean() },
                                        kind = stubSpendKinds[Random.nextInt(stubSpendKinds.size)],
                                        value = Random.nextInt(1, 10000)
                                )
                            } + (1..50).map {
                                RecordDraft(
                                        uuid = UUID.randomUUID().toString(),
                                        recordTypeId = Const.RECORD_TYPE_ID_PROFIT,
                                        date = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -Random.nextInt(5000)) }.toDateInt(),
                                        time = Calendar.getInstance().also { it.add(Calendar.MINUTE, -Random.nextInt(1440)) }.toTimeInt().takeIf { Random.nextBoolean() },
                                        kind = stubProfitKinds[Random.nextInt(stubProfitKinds.size)],
                                        value = Random.nextInt(1, 10000)
                                )
                            }
                    )
                }
                .subscribeToView()

        intent { it.clearAllClicks() }
                .doOnNext { recordsListInteractor.removeAllRecords() }
                .subscribeToView()

        RxUtils.dateChanges()
                .doOnNext { viewActions.onNext(RecordsListViewAction.RerenderAll) }
                .subscribeToView()

        recordsListInteractor.getRecordCreatedLocallyEvents()
                .doOnNext { viewActions.onNext(RecordsListViewAction.OnRecordCreatedLocally(it)) }
                .subscribeToView()

        showInfoChanges
                .skip(1) // skip initial.
                .doOnNext { recordsListInteractor.showInfo = it }
                .subscribeToView()

        super.bindIntents()
    }
}