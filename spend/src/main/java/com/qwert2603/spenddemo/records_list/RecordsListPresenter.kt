package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.records_list.entity.DateSum
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.records_list.entity.toRecordUI
import com.qwert2603.spenddemo.utils.makePair
import com.qwert2603.spenddemo.utils.onlyDate
import com.qwert2603.spenddemo.utils.plusDays
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class RecordsListPresenter @Inject constructor(
        private val recordsListInteractor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {

    override val initialState = RecordsListViewState(
            records = emptyList(),
            recordsCount = 0,
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
                            showDateSumsChanges,
                            makePair()
                    )
                    .map { (recordsList, showDateSums) ->
                        if (showDateSums) {
                            recordsList
                                    .groupBy { it.date.onlyDate() }
                                    .map { (date, records) ->
                                        records + DateSum(date, records.sumBy { it.value })
                                    }
                                    .flatten()
                        } else {
                            recordsList
                        }
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
                    recordsCount = change.records.count { it is RecordUI },
                    changesCount = change.records.count { (it as? RecordUI)?.changeKind != null },
                    balance30Days = change.records
                            .filter { it is RecordUI && it.date.onlyDate().plusDays(30) > Date().onlyDate() }
                            .sumBy { (it as RecordUI).value }
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
    }
}