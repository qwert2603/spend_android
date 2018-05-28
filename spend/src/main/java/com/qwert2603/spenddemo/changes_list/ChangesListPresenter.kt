package com.qwert2603.spenddemo.changes_list

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.base.mvi.load_refresh.list.ListPresenter
import com.qwert2603.andrlib.model.pagination.Page
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.SpendChange
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ChangesListPresenter @Inject constructor(
        private val changesListInteractor: ChangesListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : ListPresenter<Any, List<SpendChange>, ChangesListModel, ChangesListView, SpendChange>(uiSchedulerProvider) {

    override val initialState = ChangesListModel(EMPTY_LR_MODEL, EMPTY_LIST_MODEL_SINGLE_PAGE, emptyList())

    override fun ChangesListModel.applyInitialModel(i: List<SpendChange>) = copy(showingList = i)

    override fun ChangesListModel.addNextPage(nextPage: List<SpendChange>) = null!!

    override fun initialModelSingle(additionalKey: Any): Single<List<SpendChange>> = changesListInteractor.getAllChanges()

    override fun nextPageSingle(): Single<Page<SpendChange>> = Single.error(Exception("No more pages! All changes should be loaded via initialModelSingle."))

    override val partialChanges: Observable<PartialChange> = loadRefreshPartialChanges()
}