package com.qwert2603.spenddemo.changes_list

import com.qwert2603.spenddemo.base_mvi.load_refresh.LRViewState
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.ListPresenter
import com.qwert2603.spenddemo.model.entity.Change
import com.qwert2603.spenddemo.model.pagination.Page
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.utils.switchToUiIfNotYet
import io.reactivex.Single
import javax.inject.Inject

class ChangesListPresenter @Inject constructor(
        private val recordsListInteractor: ChangesListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : ListPresenter<Any, Page<Change>, ChangesListModel, ChangesListView, Change>(uiSchedulerProvider) {
    companion object {
        val INITIAL_STATE = LRViewState(false, null, false, false, null, ChangesListModel(false, null, false, emptyList()))
    }

    override fun initialModelSingle(additionalKey: Any): Single<Page<Change>> = recordsListInteractor.getAllChanges()

    override fun nextPageSingle(): Single<Page<Change>>
            = Single.error(Exception("No more pages! All changes should be loaded via initialModelSingle."))

    override fun bindIntents() {
        val observable = loadRefreshPartialChanges()
        subscribeViewState(observable.switchToUiIfNotYet(uiSchedulerProvider).scan(INITIAL_STATE, this::stateReducer).skip(1), ChangesListView::render)
    }
}