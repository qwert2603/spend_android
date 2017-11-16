package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.base_mvi.BasePresenter
import com.qwert2603.spenddemo.di.ShowChangeKinds
import com.qwert2603.spenddemo.di.ShowIds
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordsState
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.records_list.entity.toRecordUI
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.switchToUiIfNotYet
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class RecordsListPresenter @Inject constructor(
        private val recordsListInteractor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider,
        @ShowChangeKinds private val showChangeKinds: Boolean,
        @ShowIds private val showIds: Boolean
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {
    companion object {
        val INITIAL_STATE = RecordsListViewState(emptyList(), 0, 0, false, false)
    }

    override fun bindIntents() {
        val recordsStateChanges = recordsListInteractor.recordsState().share()

        intent { it.showChangesClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.MoveToChangesScreen()) }
                .subscribeToView()
        intent { it.editRecordClicks() }
                .filter { it.canEdit }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToEditRecord(it)) }
                .subscribeToView()
        intent { it.deleteRecordClicks() }
                .filter { it.canDelete }
                .doOnNext {
                    val text = "${Const.DATE_FORMAT.format(it.date)}\n${it.kind}\n${it.value}"
                    viewActions.onNext(RecordsListViewAction.AskToDeleteRecord(it.id, text))
                }
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
                .doOnNext { viewActions.onNext(RecordsListViewAction.ShowAbout()) }
                .subscribeToView()

        val observable = Observable.merge(
                recordsStateChanges
                        .map { recordsState ->
                            recordsState.records.map {
                                it.toRecordUI(recordsState.syncStatuses[it.id]!!, recordsState.changeKinds[it.id])
                            }
                        }
                        .map { RecordsListPartialChange.RecordsListUpdated(it) },
                Observable.just(showChangeKinds)
                        .map { RecordsListPartialChange.ShowChangeKinds(it) },
                Observable.just(showIds)
                        .map { RecordsListPartialChange.ShowIds(it) }
        )

        subscribeViewState(observable.switchToUiIfNotYet(uiSchedulerProvider).scan(INITIAL_STATE, this::stateReducer).skip(1), RecordsListView::render)
    }

    private fun stateReducer(viewState: RecordsListViewState, change: RecordsListPartialChange): RecordsListViewState {
        return when (change) {
            is RecordsListPartialChange.RecordsListUpdated -> viewState.copy(
                    records = change.records,
                    recordsCount = change.records.count { it is RecordUI },
                    changesCount = change.records.count { (it as? RecordUI)?.changeKind != null }
            )
            is RecordsListPartialChange.ShowChangeKinds -> viewState.copy(showChangeKinds = change.show)
            is RecordsListPartialChange.ShowIds -> viewState.copy(showIds = change.show)
        }
    }
}