package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.records_list.entity.*
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordsListPresenter @Inject constructor(
        private val recordsListInteractor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {

    override val initialState = RecordsListViewState(
            records = emptyList(),
            changesCount = 0,
            showChangeKinds = false,
            showIds = false,
            showDateSums = false,
            showProfits = false,
            showSpends = false,
            balance30Days = 0
    )

    private val viewCreated = intent { it.viewCreated() }.share()

    private val recordsStateChanges = recordsListInteractor.recordsState()
            .delaySubscription(viewCreated)
            .share()

    private val showDateSumsChanges = intent { it.showDateSumsChanges() }
            .doOnNext { recordsListInteractor.setShowDateSums(it) }
            .share()
            .startWith(recordsListInteractor.isShowDateSums())

    private val showSpendsChanges = intent { it.showSpendsChanges() }
            .doOnNext { recordsListInteractor.setShowSpends(it) }
            .share()
            .startWith(recordsListInteractor.isShowSpends())

    private val showProfitsChanges = intent { it.showProfitsChanges() }
            .doOnNext { recordsListInteractor.setShowProfits(it) }
            .share()
            .startWith(recordsListInteractor.isShowProfits())

    private val spendsListChanges = recordsStateChanges
            .map { recordsState ->
                recordsState.records.map {
                    it.toRecordUI(recordsState.syncStatuses[it.id]!!, recordsState.changeKinds[it.id])
                }
            }
            .share()

    private val reloadProfits = PublishSubject.create<Any>()

    private val profitsListChanges = Observable
            .merge(
                    viewCreated,
                    intent { it.addProfitConfirmed() }
                            .flatMapSingle {
                                recordsListInteractor
                                        .addProfit(it)
                                        .doAfterSuccess { viewActions.onNext(RecordsListViewAction.ScrollToProfitAndHighlight(it)) }
                            },
                    intent { it.deleteProfitConfirmed() }
                            .flatMapSingle { recordsListInteractor.removeProfit(it).toSingleDefault(Unit) },
                    reloadProfits
            )
            .switchMapSingle { recordsListInteractor.getAllProfits() }
            .map { it.map { it.toProfitUI() } }
            .share()

    override val partialChanges: Observable<PartialChange> = Observable.merge(listOf(
            Observable
                    .combineLatest(
                            spendsListChanges,
                            profitsListChanges,
                            showDateSumsChanges,
                            showSpendsChanges,
                            showProfitsChanges,
                            makeQuint()
                    )
                    .map { (recordsList, profitsList, showDateSums, showSpends, showProfits) ->
                        val recordsByDate = if (showSpends) recordsList.groupBy { it.date.onlyDate() } else emptyMap()
                        val profitsByDate = if (showProfits) profitsList.groupBy { it.date.onlyDate() } else emptyMap()
                        (recordsByDate.keys union profitsByDate.keys)
                                .sortedDescending()
                                .map { date ->
                                    (profitsByDate[date] plusNN recordsByDate[date])
                                            .let {
                                                if (showDateSums) {
                                                    it + DateSumUI(
                                                            date,
                                                            recordsByDate[date]?.sumByLong { it.value.toLong() },
                                                            profitsByDate[date]?.sumByLong { it.value.toLong() }
                                                    )
                                                } else {
                                                    it
                                                }
                                            }
                                }
                                .flatten()
                                .plus(createTotalsItem(recordsList, profitsList, showProfits, showSpends))
                    }
                    .map { RecordsListPartialChange.RecordsListUpdated(it) },
            intent { it.showIdsChanges() }
                    .doOnNext { recordsListInteractor.setShowIds(it) }
                    .startWith(recordsListInteractor.isShowIds())
                    .map { RecordsListPartialChange.ShowIds(it) },
            intent { it.showChangeKindsChanges() }
                    .doOnNext { recordsListInteractor.setShowChangeKinds(it) }
                    .startWith(recordsListInteractor.isShowChangeKinds())
                    .map { RecordsListPartialChange.ShowChangeKinds(it) },
            showDateSumsChanges
                    .map { RecordsListPartialChange.ShowDateSums(it) },
            showSpendsChanges
                    .map { RecordsListPartialChange.ShowSpends(it) },
            showProfitsChanges
                    .map { RecordsListPartialChange.ShowProfits(it) },
            Observable
                    .combineLatest(
                            spendsListChanges
                                    .map { it.filter { it.date.onlyDate().plusDays(30) > Date().onlyDate() } }
                                    .map { it.sumByLong { it.value.toLong() } },
                            profitsListChanges
                                    .map { it.filter { it.date.onlyDate().plusDays(30) > Date().onlyDate() } }
                                    .map { it.sumByLong { it.value.toLong() } },
                            BiFunction { spendsSum, profitsSum -> RecordsListPartialChange.Balance30DaysChanged(profitsSum - spendsSum) }
                    )
    ))

    override fun stateReducer(vs: RecordsListViewState, change: PartialChange): RecordsListViewState {
        if (change !is RecordsListPartialChange) throw Exception()
        return when (change) {
            is RecordsListPartialChange.RecordsListUpdated -> vs.copy(
                    records = change.records,
                    changesCount = change.records.count { (it as? RecordUI)?.changeKind != null }
            )
            is RecordsListPartialChange.ShowChangeKinds -> vs.copy(showChangeKinds = change.show)
            is RecordsListPartialChange.ShowIds -> vs.copy(showIds = change.show)
            is RecordsListPartialChange.ShowDateSums -> vs.copy(showDateSums = change.show)
            is RecordsListPartialChange.ShowSpends -> vs.copy(showSpends = change.show)
            is RecordsListPartialChange.ShowProfits -> vs.copy(showProfits = change.show)
            is RecordsListPartialChange.Balance30DaysChanged -> vs.copy(balance30Days = change.balance)
        }
    }

    override fun bindIntents() {
        super.bindIntents()

        intent { it.showChangesClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.MoveToChangesScreen) }
                .subscribeToView()

        intent { it.editRecordClicks() }
                .filter { it.canEdit }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToEditRecord(it)) }
                .subscribeToView()

        intent { it.deleteRecordClicks() }
                .filter { it.canDelete }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToDeleteRecord(it.id)) }
                .subscribeToView()

        intent { it.deleteRecordConfirmed() }
                .doOnNext { recordsListInteractor.deleteRecord(it) }
                .subscribeToView()

        intent { it.editRecordConfirmed() }
                .doOnNext { recordsListInteractor.editRecord(it) }
                .subscribeToView()

        recordsListInteractor.recordCreatedEvents()
                .doOnNext { viewActions.onNext(RecordsListViewAction.ScrollToRecordAndHighlight(it.id)) }
                .subscribeToView()

        intent { it.sendRecordsClicks() }
                .flatMapSingle {
                    recordsListInteractor.getRecordsTextToSend()
                            .onErrorReturn { "$it\n${it.message}" }
                }
                .doOnNext { viewActions.onNext(RecordsListViewAction.SendRecords(it)) }
                .subscribeToView()

        intent { it.showAboutClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.ShowAbout) }
                .subscribeToView()

        intent { it.addProfitClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.OpenAddProfitDialog) }
                .subscribeToView()

        intent { it.deleteProfitClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToDeleteProfit(it.id)) }
                .subscribeToView()

        intent { it.addStubSpendsClicks() }
                .flatMap {
                    val stubSpendKinds = listOf("трамвай", "столовая", "шоколадка", "автобус")
                    val random = Random()
                    Observable.interval(100, TimeUnit.MILLISECONDS)
                            .take(20)
                            .doOnNext {
                                recordsListInteractor.addRecord(CreatingRecord(
                                        kind = stubSpendKinds[random.nextInt(stubSpendKinds.size)],
                                        value = random.nextInt(1000) + 1,
                                        date = Date() - (random.nextInt(21)).days
                                ))
                            }
                }
                .subscribeToView()

        intent { it.addStubProfitsClicks() }
                .flatMap {
                    val stubProfitKinds = listOf("стипендия", "зарплата", "аванс", "доход")
                    val random = Random()
                    Observable.range(0, 200)
                            .concatMapSingle {
                                recordsListInteractor.addProfit(CreatingProfit(
                                        kind = stubProfitKinds[random.nextInt(stubProfitKinds.size)],
                                        value = random.nextInt(10000) + 1,
                                        date = Date() - (random.nextInt(21)).days
                                ))
                            }
                            .doAfterTerminate { reloadProfits.onNext(Any()) }
                }
                .subscribeToView()

        intent { it.clearAllClicks() }
                .flatMap {
                    recordsListInteractor.removeAllProfits()
                            .doOnComplete { recordsListInteractor.deleteAllRecords() }
                            .toObservable<Any>()
                            .doAfterTerminate { reloadProfits.onNext(Any()) }
                }
                .subscribeToView()
    }

    private fun createTotalsItem(
            recordsList: List<RecordUI>,
            profitsList: List<ProfitUI>,
            showProfits: Boolean,
            showSpends: Boolean
    ): TotalsUi {
        val spendsSum = recordsList.sumByLong { it.value.toLong() }
        val profitsSum = profitsList.sumByLong { it.value.toLong() }
        return TotalsUi(
                showProfits = showProfits,
                showSpends = showSpends,
                spendsCount = recordsList.size,
                spendsSum = spendsSum,
                profitsCount = profitsList.size,
                profitsSum = profitsSum,
                totalBalance = profitsSum - spendsSum
        )
    }
}