package com.qwert2603.spenddemo.base_mvi.load_refresh.list

import com.qwert2603.spenddemo.base_mvi.PartialChange
import com.qwert2603.spenddemo.base_mvi.load_refresh.InitialModelHolder
import com.qwert2603.spenddemo.base_mvi.load_refresh.LRPartialChange
import com.qwert2603.spenddemo.base_mvi.load_refresh.LRPresenter
import com.qwert2603.spenddemo.base_mvi.load_refresh.LRViewState
import com.qwert2603.spenddemo.model.entity.IdentifiableLong
import com.qwert2603.spenddemo.model.pagination.Page
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.utils.cancelOn
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Presenter that loads list of items and allows pagination.
 */
abstract class ListPresenter<A, I, M, V : ListView<M>, T : IdentifiableLong>(uiSchedulerProvider: UiSchedulerProvider)
    : LRPresenter<A, I, M, V>(uiSchedulerProvider)
        where M : InitialModelHolder<I>, M : ListModelHolder<T> {

    /**
     * Load next page after previous page was loaded or retry loading page.
     */
    protected val loadNextPageIntent: Observable<Any> = intent { it.loadNextPage() }

    protected abstract fun nextPageSingle(): Single<Page<T>>

    protected fun paginationChanges(): Observable<PartialChange> = loadNextPageIntent
            .switchMap {
                /**
                 * Add [.subscribeWith(PublishSubject.create<Any>())] because [reloadIntent] may be [BehaviorSubject]
                 * and emit item immediately that triggers cancelling.
                 * We need to cancel ONLY if item is emitted while loading next page.
                 */
                nextPageSingle()
                        .toObservable()
                        .map<PartialChange> { ListPartialChange.NextPageLoaded(it) }
                        .onErrorReturn { ListPartialChange.NextPageError(it) }
                        .startWith(ListPartialChange.NextPageLoading())
                        .cancelOn(Observable.merge(reloadIntent, retryIntent, refreshIntent).subscribeWith(PublishSubject.create<Any>()), ListPartialChange.NextPageCancelled())
            }

    @Suppress("UNCHECKED_CAST")
    override fun stateReducer(viewState: LRViewState<M>, change: PartialChange): LRViewState<M> {
        if (change !is ListPartialChange) return super.stateReducer(viewState, change)
                .let {
                    if (change is LRPartialChange.InitialModelLoaded<*>) {
                        it.copy(model = it.model.changeListModel(
                                nextPageList = null,
                                nextPageLoading = false,
                                nextPageError = null,
                                allItemsLoaded = it.model.allItemsLoaded
                        ) as M)
                    } else it
                }
        return when (change) {
            is ListPartialChange.NextPageLoading -> viewState.copy(model = viewState.model.changeListModel(
                    nextPageList = null,
                    nextPageLoading = true,
                    nextPageError = null,
                    allItemsLoaded = false
            ) as M)
            is ListPartialChange.NextPageError -> viewState.copy(model = viewState.model.changeListModel(
                    nextPageList = null,
                    nextPageLoading = false,
                    nextPageError = change.t,
                    allItemsLoaded = false
            ) as M)
            is ListPartialChange.NextPageCancelled -> viewState.copy(model = viewState.model.changeListModel(
                    nextPageList = null,
                    nextPageLoading = false,
                    nextPageError = null,
                    allItemsLoaded = false
            ) as M)
            is ListPartialChange.NextPageLoaded<*> -> viewState.copy(model = viewState.model.changeListModel(
                    nextPageList = change.page.list as List<T>,
                    nextPageLoading = false,
                    nextPageError = null,
                    allItemsLoaded = change.page.allItemsLoaded
            ) as M)
        }
    }
}