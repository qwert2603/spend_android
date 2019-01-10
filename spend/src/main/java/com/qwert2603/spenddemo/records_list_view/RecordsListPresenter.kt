package com.qwert2603.spenddemo.records_list_view

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.Record
import io.reactivex.Observable
import javax.inject.Inject

class RecordsListPresenter @Inject constructor(
        recordsUuids: List<String>,
        interactor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {

    override val initialState = RecordsListViewState(null)

    private data class RecordsListChanged(val records: List<Record>) : PartialChange

    override val partialChanges: Observable<PartialChange> = interactor
            .getRecordsList(recordsUuids)
            .map { RecordsListChanged(it) }

    override fun stateReducer(vs: RecordsListViewState, change: PartialChange): RecordsListViewState {
        if (change !is RecordsListChanged) throw Exception()
        return vs.copy(records = change.records)
    }
}