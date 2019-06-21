package com.qwert2603.spend.records_list_view

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spend.model.entity.Record
import com.qwert2603.spend.utils.RxUtils
import io.reactivex.Observable

class RecordsListPresenter(
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

    override fun bindIntents() {
        RxUtils.dateChanges()
                .doOnNext { viewActions.onNext(RecordsListViewAction.RerenderAll) }
                .subscribeToView()

        super.bindIntents()
    }
}