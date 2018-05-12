package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.records_list.entity.SpendUI
import com.qwert2603.spenddemo.records_list.entity.toProfitUI
import com.qwert2603.spenddemo.records_list.entity.toSpendUI
import com.qwert2603.spenddemo.utils.makeSextuple
import com.qwert2603.spenddemo.utils.onlyDate
import com.qwert2603.spenddemo.utils.plusDays
import com.qwert2603.spenddemo.utils.sumByLong
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import java.util.*
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
            showMonthSums = false,
            showProfits = false,
            showSpends = false,
            balance30Days = 0
    )

    private val viewCreated = intent { it.viewCreated() }.share()

    private val showDateSumsChanges = intent { it.showDateSumsChanges() }
            .doOnNext { recordsListInteractor.setShowDateSums(it) }
            .share()
            .startWith(recordsListInteractor.isShowDateSums())

    private val showMonthSumsChanges = intent { it.showMonthSumsChanges() }
            .doOnNext { recordsListInteractor.setShowMonthSums(it) }
            .share()
            .startWith(recordsListInteractor.isShowMonthSums())

    private val showSpendsChanges = intent { it.showSpendsChanges() }
            .doOnNext { recordsListInteractor.setShowSpends(it) }
            .share()
            .startWith(recordsListInteractor.isShowSpends())

    private val showProfitsChanges = intent { it.showProfitsChanges() }
            .doOnNext { recordsListInteractor.setShowProfits(it) }
            .share()
            .startWith(recordsListInteractor.isShowProfits())

    private val spendsListChanges = recordsListInteractor.spendsState()
            .delaySubscription(viewCreated)
            .map { spendsState ->
                spendsState.spends.map {
                    it.toSpendUI(spendsState.syncStatuses[it.id]!!, spendsState.changeKinds[it.id])
                }
            }
            .share()

    private val reloadProfits = PublishSubject.create<Any>()

    private val profitsListChanges = Observable
            .merge(listOf(
                    viewCreated,
                    intent { it.addProfitConfirmed() }
                            .flatMapSingle {
                                recordsListInteractor
                                        .addProfit(it)
                                        .doAfterSuccess { viewActions.onNext(RecordsListViewAction.ScrollToProfitAndHighlight(it)) }
                            },
                    intent { it.editProfitConfirmed() }
                            .flatMapSingle { recordsListInteractor.editProfit(it).toSingleDefault(Unit) },
                    intent { it.deleteProfitConfirmed() }
                            .flatMapSingle { recordsListInteractor.removeProfit(it).toSingleDefault(Unit) },
                    reloadProfits
            ))
            .switchMapSingle { recordsListInteractor.getAllProfits() }
            .map { it.map { it.toProfitUI() } }
            .share()

    override val partialChanges: Observable<PartialChange> = Observable.merge(listOf(
            Observable
                    .combineLatest(
                            spendsListChanges,
                            profitsListChanges,
                            showDateSumsChanges,
                            showMonthSumsChanges,
                            showSpendsChanges,
                            showProfitsChanges,
                            makeSextuple()
                    )
                    .map { (spendsList, profitsList, showDateSums, showMonthSums, showSpends, showProfits) ->
                        makeRecordsList(spendsList, profitsList, showDateSums, showMonthSums, showProfits, showSpends)
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
            showMonthSumsChanges
                    .map { RecordsListPartialChange.ShowMonthSums(it) },
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
                    changesCount = change.records.count { (it as? SpendUI)?.changeKind != null }
            )
            is RecordsListPartialChange.ShowChangeKinds -> vs.copy(showChangeKinds = change.show)
            is RecordsListPartialChange.ShowIds -> vs.copy(showIds = change.show)
            is RecordsListPartialChange.ShowDateSums -> vs.copy(showDateSums = change.show)
            is RecordsListPartialChange.ShowMonthSums -> vs.copy(showMonthSums = change.show)
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

        intent { it.editSpendClicks() }
                .filter { it.canEdit }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToEditSpend(it)) }
                .subscribeToView()

        intent { it.deleteSpendClicks() }
                .filter { it.canDelete }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToDeleteSpend(it.id)) }
                .subscribeToView()

        intent { it.deleteSpendConfirmed() }
                .doOnNext { recordsListInteractor.deleteSpend(it) }
                .subscribeToView()

        intent { it.editSpendConfirmed() }
                .doOnNext { recordsListInteractor.editSpend(it) }
                .subscribeToView()

        recordsListInteractor.spendCreatedEvents()
                .doOnNext { viewActions.onNext(RecordsListViewAction.ScrollToSpendAndHighlight(it.id)) }
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

        intent { it.editProfitClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToEditProfit(it)) }
                .subscribeToView()

        intent { it.deleteProfitClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToDeleteProfit(it.id)) }
                .subscribeToView()

        intent { it.addStubSpendsClicks() }
                .flatMap { addStubSpends(recordsListInteractor, 20) }
                .subscribeToView()

        intent { it.addStubProfitsClicks() }
                .flatMap {
                    addStubProfits(recordsListInteractor, 200)
                            .doAfterTerminate { reloadProfits.onNext(Any()) }
                }
                .subscribeToView()

        intent { it.clearAllClicks() }
                .flatMap {
                    recordsListInteractor.removeAllProfits()
                            .doOnComplete { recordsListInteractor.deleteAllSpends() }
                            .toObservable<Any>()
                            .doAfterTerminate { reloadProfits.onNext(Any()) }
                }
                .subscribeToView()
    }
}