package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashSet
import kotlin.random.Random

class RecordsListPresenter @Inject constructor(
        private val recordsListInteractor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {

    override val initialState = RecordsListViewState(
            records = null,
            diff = FastDiffUtils.FastDiffResult.EMPTY,
            showInfo = recordsListInteractor.showInfo.field,
            longSumPeriod = recordsListInteractor.longSumPeriod.field,
            shortSumPeriod = recordsListInteractor.shortSumPeriod.field,
            sumsInfo = SumsInfo.EMPTY,
            recordsChanges = hashMapOf(),
            selectedRecordsUuids = hashSetOf(),
            syncState = SyncState.SYNCING
    )

    private val showInfoChanges: Observable<ShowInfo> = recordsListInteractor.showInfo.changes.shareAfterViewSubscribed()

    private val longSumPeriodChanges: Observable<Days> = recordsListInteractor.longSumPeriod.changes.shareAfterViewSubscribed()

    private val shortSumPeriodChanges: Observable<Minutes> = recordsListInteractor.shortSumPeriod.changes.shareAfterViewSubscribed()

    override val partialChanges: Observable<PartialChange> = Observable.merge(listOf(
            showInfoChanges
                    .map { RecordsListPartialChange.ShowInfoChanged(it) },
            Observable
                    .combineLatest(
                            showInfoChanges,
                            longSumPeriodChanges,
                            shortSumPeriodChanges,
                            viewStateObservable
                                    .map { it.selectedRecordsUuids }
                                    .distinctUntilChanged(),
                            makeQuad()
                    )
                    .switchMap { quad ->
                        RxUtils.minuteChanges()
                                .startWith(Any())
                                .map { quad }
                    }
                    .switchMap { (showInfo, longSumPeriodDays, shortSumPeriodMinutes, selectedUuids) ->
                        recordsListInteractor.getRecordsList()
                                .map { it.toRecordItemsList(showInfo, longSumPeriodDays, shortSumPeriodMinutes, selectedUuids) }
                    }
                    .startWith(emptyList<RecordsListItem>())
                    .buffer(2, 1)
                    .map { (prev, current) ->
                        val diffResult = FastDiffUtils.fastCalculateDiff(
                                oldList = prev,
                                newList = current,
                                id = RecordsListItem::idInList,
                                compareOrder = RecordsListItem.COMPARE_ORDER,
                                isEqual = RecordsListItem.IS_EQUAL
                        )
                        val recordsChanges = hashMapOf<String, RecordChange>()
                        current.forEach {
                            if (it is Record && it.change != null) {
                                recordsChanges[it.uuid] = it.change
                            }
                        }
                        RecordsListPartialChange.RecordsListChanged(
                                list = current,
                                diff = diffResult,
                                recordsChanges = recordsChanges
                        )
                    },
            longSumPeriodChanges
                    .map { RecordsListPartialChange.LongSumPeriodChanged(it) },
            shortSumPeriodChanges
                    .map { RecordsListPartialChange.ShortSumPeriodChanged(it) },
            sumsInfoChanges(
                    longSumPeriodChanges,
                    shortSumPeriodChanges,
                    showInfoChanges,
                    recordsListInteractor
            ).map { RecordsListPartialChange.SumsInfoChanged(it) },
            recordsListInteractor
                    .getSyncState()
                    .modifyForUi()
                    .map { RecordsListPartialChange.SyncStateChanged(it) },
            intent { it.recordLongClicks() }
                    .filter { !it.isDeleted() }
                    .map { RecordsListPartialChange.ToggleRecordSelection(it.uuid) },
            intent { it.recordClicks() }
                    .filter { !it.isDeleted() }
                    .withLatestFrom(viewStateObservable, makePair())
                    .filter { (record, vs) ->
                        if (!vs.selectMode) {
                            viewActions.onNext(RecordsListViewAction.AskForRecordActions(record.uuid))
                        }
                        vs.selectMode
                    }
                    .map { (record, _) -> RecordsListPartialChange.ToggleRecordSelection(record.uuid) },
            intent { it.cancelSelection() }
                    .map { RecordsListPartialChange.ClearSelection }
    ))

    override fun stateReducer(vs: RecordsListViewState, change: PartialChange): RecordsListViewState {
        if (change !is RecordsListPartialChange) throw Exception()
        return when (change) {
            is RecordsListPartialChange.RecordsListChanged -> vs.copy(
                    records = change.list,
                    diff = change.diff,
                    recordsChanges = change.recordsChanges
            ).let { changedVS ->
                changedVS.copy(selectedRecordsUuids = changedVS.selectedRecordsUuids
                        .filterTo(HashSet()) { changedVS.recordsByUuid?.get(it)?.isDeleted() == false }
                )
            }
            is RecordsListPartialChange.ShowInfoChanged -> vs.copy(showInfo = change.showInfo)
            is RecordsListPartialChange.SumsInfoChanged -> vs.copy(sumsInfo = change.sumsInfo)
            is RecordsListPartialChange.LongSumPeriodChanged -> vs.copy(longSumPeriod = change.days)
            is RecordsListPartialChange.ShortSumPeriodChanged -> vs.copy(shortSumPeriod = change.minutes)
            is RecordsListPartialChange.SyncStateChanged -> vs.copy(syncState = change.syncState)
            is RecordsListPartialChange.ToggleRecordSelection -> vs.copy(selectedRecordsUuids = vs.selectedRecordsUuids.toggleAndFilter(
                    itemToToggle = change.recordUuid,
                    filter = { vs.recordsByUuid?.get(it)?.isDeleted() == false }
            ))
            RecordsListPartialChange.ClearSelection -> vs.copy(selectedRecordsUuids = hashSetOf())
        }
    }

    override fun bindIntents() {

        Observable
                .merge(listOf(
                        intent { it.showSpendsChanges() }.map { ShowInfoChange.Spends(it) },
                        intent { it.showProfitsChanges() }.map { ShowInfoChange.Profits(it) },
                        intent { it.showSumsChanges() }.map { ShowInfoChange.Sums(it) },
                        intent { it.showChangeKindsChanges() }.map { ShowInfoChange.ChangeKinds(it) },
                        intent { it.showTimesChanges() }.map { ShowInfoChange.Times(it) }
                ))
                .doOnNext { ch ->
                    recordsListInteractor.showInfo.updateField { info ->
                        return@updateField when (ch) {
                            is ShowInfoChange.Spends -> info.copy(showSpends = ch.show)
                            is ShowInfoChange.Profits -> info.copy(showProfits = ch.show)
                            is ShowInfoChange.Sums -> info.copy(showSums = ch.show)
                            is ShowInfoChange.ChangeKinds -> info.copy(showChangeKinds = ch.show)
                            is ShowInfoChange.Times -> info.copy(showTimes = ch.show)
                        }
                    }
                }
                .subscribeToView()

        intent { it.longSumPeriodSelected() }
                .doOnNext { recordsListInteractor.longSumPeriod.field = it }
                .subscribeToView()

        intent { it.shortSumPeriodSelected() }
                .doOnNext { recordsListInteractor.shortSumPeriod.field = it }
                .subscribeToView()

        intent { it.createProfitClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToCreateRecord(Const.RECORD_TYPE_ID_PROFIT)) }
                .subscribeToView()

        intent { it.chooseLongSumPeriodClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToChooseLongSumPeriod(it.longSumPeriod)) }
                .subscribeToView()

        intent { it.chooseShortSumPeriodClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToChooseShortSumPeriod(it.shortSumPeriod)) }
                .subscribeToView()

        intent { it.editRecordClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToEditRecord(it)) }
                .subscribeToView()

        intent { it.deleteRecordClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToDeleteRecord(it)) }
                .subscribeToView()

        intent { it.addStubRecordsClicks() }
                .flatMapSingle {
                    Single.zip(
                            recordsListInteractor.getRecordCategories(Const.RECORD_TYPE_ID_SPEND),
                            recordsListInteractor.getRecordCategories(Const.RECORD_TYPE_ID_PROFIT),
                            makePair()
                    )
                }
                .doOnNext { (spendCategories, profitCategories) ->

                    val stubSpendKinds = listOf("трамвай", "столовая", "шоколадка", "автобус")
                    val stubProfitKinds = listOf("стипендия", "зарплата", "аванс", "доход")

                    val spends = if (spendCategories.isNotEmpty()) {
                        (1..200).map {
                            val recordCategory = spendCategories.random()
                            RecordDraft(
                                    isNewRecord = true,
                                    uuid = UUID.randomUUID().toString(),
                                    recordTypeId = recordCategory.recordTypeId,
                                    recordCategoryName = recordCategory.name,
                                    recordCategoryUuid = recordCategory.uuid,
                                    date = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -Random.nextInt(5000)) }.toSDate(),
                                    time = Calendar.getInstance().also { it.add(Calendar.MINUTE, -Random.nextInt(1440)) }.toSTime().takeIf { Random.nextBoolean() },
                                    kind = stubSpendKinds[Random.nextInt(stubSpendKinds.size)],
                                    value = Random.nextInt(1, 10000)
                            )
                        }
                    } else {
                        emptyList()
                    }
                    val profits = if (profitCategories.isNotEmpty()) {
                        (1..50).map {
                            val recordCategory = profitCategories.random()
                            RecordDraft(
                                    isNewRecord = true,
                                    uuid = UUID.randomUUID().toString(),
                                    recordTypeId = recordCategory.recordTypeId,
                                    recordCategoryName = recordCategory.name,
                                    recordCategoryUuid = recordCategory.uuid,
                                    date = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -Random.nextInt(5000)) }.toSDate(),
                                    time = Calendar.getInstance().also { it.add(Calendar.MINUTE, -Random.nextInt(1440)) }.toSTime().takeIf { Random.nextBoolean() },
                                    kind = stubProfitKinds[Random.nextInt(stubProfitKinds.size)],
                                    value = Random.nextInt(1, 10000)
                            )
                        }
                    } else {
                        emptyList()
                    }
                    recordsListInteractor.addRecords(spends + profits)
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

        recordsListInteractor.getRecordEditedLocallyEvents()
                .doOnNext { viewActions.onNext(RecordsListViewAction.OnRecordEditedLocally(it)) }
                .subscribeToView()

        super.bindIntents()
    }

    private sealed class ShowInfoChange {
        data class Spends(val show: Boolean) : ShowInfoChange()
        data class Profits(val show: Boolean) : ShowInfoChange()
        data class Sums(val show: Boolean) : ShowInfoChange()
        data class ChangeKinds(val show: Boolean) : ShowInfoChange()
        data class Times(val show: Boolean) : ShowInfoChange()
    }
}