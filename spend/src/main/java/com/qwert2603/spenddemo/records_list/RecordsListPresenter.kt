package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.records_list.entity.*
import com.qwert2603.spenddemo.utils.makeTriple
import com.qwert2603.spenddemo.utils.onlyDate
import com.qwert2603.spenddemo.utils.plusNN
import io.reactivex.Observable
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

    override val partialChanges: Observable<PartialChange> = Observable.merge(listOf(
            Observable
                    .combineLatest(
                            recordsStateChanges
                                    .map { recordsState ->
                                        recordsState.records.map {
                                            it.toRecordUI(recordsState.syncStatuses[it.id]!!, recordsState.changeKinds[it.id])
                                        }
                                    },
                            Observable
                                    .merge(
                                            viewCreated,
                                            intent { it.addProfitConfirmed() }
                                                    .flatMapSingle { recordsListInteractor.addProfit(it).toSingleDefault(Unit) },
                                            intent { it.deleteProfitConfirmed() }
                                                    .flatMapSingle { recordsListInteractor.removeProfit(it).toSingleDefault(Unit) }
                                    )
                                    .switchMapSingle { recordsListInteractor.getAllProfits() }
                                    .map { it.map { it.toProfitUI() } },
                            showDateSumsChanges,
                            makeTriple()
                    )
                    .map { (recordsList, profitsList, showDateSums) ->
                        val recordsByDate = recordsList.groupBy { it.date.onlyDate() }
                        val profitsByDate = profitsList.groupBy { it.date.onlyDate() }
                        (recordsByDate.keys union profitsByDate.keys)
                                .sortedDescending()
                                .map { date ->
                                    (profitsByDate[date] plusNN recordsByDate[date])
                                            .let {
                                                if (showDateSums) {
                                                    it + DateSumUI(
                                                            date,
                                                            recordsByDate[date]?.sumBy { it.value }
                                                                    ?: 0,
                                                            profitsByDate[date]?.sumBy { it.value }
                                                                    ?: 0
                                                    )
                                                } else {
                                                    it
                                                }
                                            }
                                }
                                .flatten()
                                .addTotalsItem()
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
                    .map { RecordsListPartialChange.ShowDateSums(it) }
    ))

    override fun stateReducer(vs: RecordsListViewState, change: PartialChange): RecordsListViewState {
        if (change !is RecordsListPartialChange) throw Exception()
        return when (change) {
            is RecordsListPartialChange.RecordsListUpdated -> vs.copy(
                    records = change.records,
                    changesCount = change.records.count { (it as? RecordUI)?.changeKind != null },
                    balance30Days = change.records
                            .sumBy {
                                when (it) {
                                    is RecordUI -> -it.value
                                    is ProfitUI -> it.value
                                    else -> 0
                                }
                            }
            )
            is RecordsListPartialChange.ShowChangeKinds -> vs.copy(showChangeKinds = change.show)
            is RecordsListPartialChange.ShowIds -> vs.copy(showIds = change.show)
            is RecordsListPartialChange.ShowDateSums -> vs.copy(showDateSums = change.show)
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
                .flatMapSingle { recordsListInteractor.getRecordsTextToSend() }
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
    }

    private fun List<RecordsListItem>.addTotalsItem(): List<RecordsListItem> {
        val spends = mapNotNull { it as? RecordUI }
        val profits = mapNotNull { it as? ProfitUI }
        val spendsSum = spends.sumBy { it.value }
        val profitsSum = profits.sumBy { it.value }
        return this + TotalsUi(
                spendsCount = spends.size,
                spendsSum = spendsSum,
                profitsCount = profits.size,
                profitsSum = profitsSum,
                totalBalance = profitsSum - spendsSum
        )
    }
}