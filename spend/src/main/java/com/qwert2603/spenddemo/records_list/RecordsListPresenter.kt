package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.base_mvi.BasePresenter
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordsState
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.records_list.entity.toRecordUI
import com.qwert2603.spenddemo.utils.switchToUiIfNotYet
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class RecordsListPresenter @Inject constructor(
        private val recordsListInteractor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {
    companion object {
        val INITIAL_STATE = RecordsListViewState(emptyList(), 0, 0)
    }

    override fun bindIntents() {
        intent { it.showChangesClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.MoveToChangesScreen()) }
                .subscribeToView()
        intent { it.editRecordClicks() }
                .filter { it.canEdit }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToEditRecord(it)) }
                .subscribeToView()
        intent { it.deleteRecordClicks() }
                .filter { it.canDelete }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToDeleteRecord(it.id, it.toString())) }
                .subscribeToView()
        intent { it.deleteRecordConfirmed() }
                .doOnNext { recordsListInteractor.deleteRecord(it) }
                .subscribeToView()
        intent { it.editRecordConfirmed() }
                .doOnNext { recordsListInteractor.editRecord(it) }
                .subscribeToView()
        recordsListInteractor.recordCreatedEvents()
                .withLatestFrom(
                        recordsListInteractor.recordsState(),
                        BiFunction { record: Record, recordsState: RecordsState ->
                            recordsState.records.indexOf(record)
                        }
                )
                .filter { it >= 0 }
                .switchToUiIfNotYet(uiSchedulerProvider)
                .doOnNext { viewActions.onNext(RecordsListViewAction.ScrollToPosition(it)) }
                .subscribeToView()

        val observable = recordsListInteractor.recordsState()
                .map { recordsState ->
                    recordsState.records.map {
                        it.toRecordUI(recordsState.syncStatuses[it.id]!!, recordsState.changeKinds[it.id])
                    }
                }
//                .map { listOf(AddRecordItem) + it }
                .map { RecordsListPartialChange.RecordsListUpdated(it) }

        subscribeViewState(observable.switchToUiIfNotYet(uiSchedulerProvider).scan(INITIAL_STATE, this::stateReducer).skip(1), RecordsListView::render)
    }

    private fun stateReducer(viewState: RecordsListViewState, change: RecordsListPartialChange): RecordsListViewState {
        return when (change) {
            is RecordsListPartialChange.RecordsListUpdated -> viewState.copy(
                    records = change.records,
                    recordsCount = change.records.count { it is RecordUI },
                    changesCount = change.records.count { (it as? RecordUI)?.changeKind != null }
            )
        }
    }
}