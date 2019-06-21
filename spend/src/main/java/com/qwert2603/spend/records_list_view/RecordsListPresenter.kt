package com.qwert2603.spend.records_list_view

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spend.model.entity.OldRecordsLockState
import com.qwert2603.spend.model.entity.Record
import com.qwert2603.spend.utils.RxUtils
import io.reactivex.Observable

class RecordsListPresenter(
        recordsUuids: List<String>,
        interactor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {

    override val initialState = RecordsListViewState(null, true)

    private sealed class RecordsListPartialChange : PartialChange {
        data class RecordsListChanged(val records: List<Record>) : RecordsListPartialChange()
        data class OldRecordsLockStateChanged(val oldRecordsLockState: OldRecordsLockState) : RecordsListPartialChange()
    }

    override val partialChanges: Observable<PartialChange> = Observable.merge(
            interactor
                    .getRecordsList(recordsUuids)
                    .map { RecordsListPartialChange.RecordsListChanged(it) },
            interactor
                    .oldRecordsLockStateChanges()
                    .map { RecordsListPartialChange.OldRecordsLockStateChanged(it) }
    )

    override fun stateReducer(vs: RecordsListViewState, change: PartialChange): RecordsListViewState {
        if (change !is RecordsListPartialChange) throw Exception()
        return when (change) {
            is RecordsListPartialChange.RecordsListChanged -> vs.copy(records = change.records)
            is RecordsListPartialChange.OldRecordsLockStateChanged -> vs.copy(oldRecordsLock = change.oldRecordsLockState.isLocked)
        }
    }

    override fun bindIntents() {
        RxUtils.dateChanges()
                .doOnNext { viewActions.onNext(RecordsListViewAction.RerenderAll) }
                .subscribeToView()

        super.bindIntents()
    }
}