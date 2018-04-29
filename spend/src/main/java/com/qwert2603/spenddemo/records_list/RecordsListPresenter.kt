package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.di.qualifiers.ShowChangeKinds
import com.qwert2603.spenddemo.di.qualifiers.ShowIds
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordsState
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.records_list.entity.toRecordUI
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class RecordsListPresenter @Inject constructor(
        private val recordsListInteractor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider,
        @ShowChangeKinds showChangeKinds: Boolean,
        @ShowIds showIds: Boolean
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {

    override val initialState = RecordsListViewState(emptyList(), 0, 0, false, false)

    private val viewCreated = intent { it.viewCreated() }.share()

    private val recordsStateChanges = recordsListInteractor.recordsState()
            .delaySubscription(viewCreated)
            .share()

    override val partialChanges: Observable<PartialChange> = Observable.merge(
            recordsStateChanges
                    .map { recordsState ->
                        recordsState.records.map {
                            it.toRecordUI(recordsState.syncStatuses[it.id]!!, recordsState.changeKinds[it.id])
                        }
                    }
                    .map { RecordsListPartialChange.RecordsListUpdated(it) },
            Observable.just(showChangeKinds)
                    .delaySubscription(viewCreated)
                    .map { RecordsListPartialChange.ShowChangeKinds(it) },
            Observable.just(showIds)
                    .delaySubscription(viewCreated)
                    .map { RecordsListPartialChange.ShowIds(it) }
    )

    override fun stateReducer(vs: RecordsListViewState, change: PartialChange): RecordsListViewState {
        if (change !is RecordsListPartialChange) throw Exception()
        return when (change) {
            is RecordsListPartialChange.RecordsListUpdated -> vs.copy(
                    records = change.records,
                    recordsCount = change.records.count { it is RecordUI },
                    changesCount = change.records.count { (it as? RecordUI)?.changeKind != null }
            )
            is RecordsListPartialChange.ShowChangeKinds -> vs.copy(showChangeKinds = change.show)
            is RecordsListPartialChange.ShowIds -> vs.copy(showIds = change.show)
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
                .withLatestFrom(
                        recordsStateChanges,
                        BiFunction { record: Record, recordsState: RecordsState ->
                            recordsState.records.indexOf(record)
                        }
                )
                .filter { it >= 0 }
                .doOnNext { viewActions.onNext(RecordsListViewAction.ScrollToPosition(it)) }
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