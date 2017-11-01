package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.base_mvi.BasePresenter
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.records_list.entity.AddRecordItem
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.records_list.entity.toRecordUI
import com.qwert2603.spenddemo.utils.switchToUiIfNotYet
import javax.inject.Inject

class RecordsListPresenter @Inject constructor(
        private val recordsListInteractor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {
    companion object {
        val INITIAL_STATE = RecordsListViewState(listOf(AddRecordItem), 0)
    }

    override fun bindIntents() {
        intent { it.showChangesClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.MoveToChangesScreen()) }
                .subscribeToView()
        intent { it.recordClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToEditRecord(it)) }
                .subscribeToView()
        intent { it.recordLongClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToDeleteRecord(it.id, it.toString())) }
                .subscribeToView()
        intent { it.deleteRecordConfirmed() }
                .flatMap {
                    recordsListInteractor.deleteRecord(it)
                            .onErrorComplete()
                            .toObservable<Any>()
                }
                .subscribeToView()
        intent { it.editRecordConfirmed() }
                .flatMap {
                    recordsListInteractor.editRecord(it)
                            .onErrorComplete()
                            .toObservable<Any>()
                }
                .subscribeToView()

        val observable = recordsListInteractor.recordsState()
                .map { recordsState ->
                    recordsState.records.map {
                        it.toRecordUI(recordsState.syncStatuses[it.id]!!, recordsState.changeKinds[it.id])
                    }
                }
                .map { listOf(AddRecordItem) + it }
                .map { RecordsListPartialChange.RecordsListUpdated(it) }

        subscribeViewState(observable.switchToUiIfNotYet(uiSchedulerProvider).scan(INITIAL_STATE, this::stateReducer).skip(1), RecordsListView::render)
    }

    private fun stateReducer(viewState: RecordsListViewState, change: RecordsListPartialChange): RecordsListViewState {
        return when (change) {
            is RecordsListPartialChange.RecordsListUpdated -> viewState.copy(
                    records = change.records,
                    recordsCount = change.records.count { it is RecordUI }
            )
        }
    }
}