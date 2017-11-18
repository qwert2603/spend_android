package com.qwert2603.spenddemo.base_mvi.load_refresh

import android.support.annotation.CallSuper
import com.qwert2603.spenddemo.base_mvi.BasePresenter
import com.qwert2603.spenddemo.base_mvi.PartialChange
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.utils.LogUtils
import com.qwert2603.spenddemo.utils.cancelOn
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Presenter that can load (retry load) and refresh initial model.
 */
abstract class LRPresenter<A, I, M : InitialModelHolder<I>, V : LRView<M>>(uiSchedulerProvider: UiSchedulerProvider)
    : BasePresenter<V, LRViewState<M>>(uiSchedulerProvider) {

    protected abstract fun initialModelSingle(additionalKey: A): Single<I>

    /**
     * Classes-inheritors may override it to trigger reload intent when needed.
     * Reload means that initial model will be reloaded.
     */
    open protected val reloadIntent: Observable<Any> = Observable.never()

    /**
     * Load initial model.
     */
    protected val loadIntent: Observable<Any> = intent { it.load() }

    /**
     * Reload initial model after error loading it.
     */
    protected val retryIntent: Observable<Any> = intent { it.retry() }

    /**
     * Refresh initial model after it was loaded successfully.
     * While refreshing already loaded initial model will be showing.
     */
    protected val refreshIntent: Observable<Any> = intent { it.refresh() }

    /**
     * @return Observable that emits every time when loading initial model.
     */
    protected fun initialModelLoading(): Observable<Any> = Observable.merge(
            Observable.combineLatest(
                    loadIntent,
                    reloadIntent.startWith(Any()),
                    BiFunction { a, _ -> a }
            ),
            retryIntent,
            refreshIntent
    )

    /** [additionalKey] is additional key that can be used for loading initial model. */
    @Suppress("UNCHECKED_CAST")
    protected fun loadRefreshPartialChanges(additionalKey: Observable<A> = Observable.just(Any() as A)): Observable<LRPartialChange> = Observable.merge(
            Observable
                    .merge(
                            Observable.combineLatest(
                                    loadIntent,
                                    reloadIntent.startWith(Any()),
                                    BiFunction { k, _ -> k }
                            ),
                            retryIntent
                    )
                    .withLatestFrom(additionalKey, BiFunction { _: Any, a: A -> a })
                    .switchMap {
                        initialModelSingle(it)
                                .toObservable()
                                .map<LRPartialChange> { LRPartialChange.InitialModelLoaded(it) }
                                .onErrorReturn { LRPartialChange.LoadingError(it) }
                                .startWith(LRPartialChange.LoadingStarted())
                    },
            refreshIntent
                    .withLatestFrom(additionalKey, BiFunction { _: Any, a: A -> a })
                    .switchMap {
                        /**
                         * Add [.subscribeWith(PublishSubject.create<Any>())] because [reloadIntent] may be [BehaviorSubject]
                         * and emit item immediately that triggers cancelling.
                         * We need to cancel ONLY if item is emitted while refreshing.
                         */
                        initialModelSingle(it)
                                .toObservable()
                                .map<LRPartialChange> { LRPartialChange.InitialModelLoaded(it) }
                                .onErrorReturn { LRPartialChange.RefreshError(it) }
                                .startWith(LRPartialChange.RefreshStarted())
                                .cancelOn(Observable.merge(reloadIntent, retryIntent).subscribeWith(PublishSubject.create<Any>()), LRPartialChange.RefreshCancelled())
                    }
    )

    @CallSuper
    open protected fun stateReducer(viewState: LRViewState<M>, change: PartialChange): LRViewState<M> {
        LogUtils.d { "LRPresenter ${this.javaClass.simpleName} stateReducer $change" }
        if (change !is LRPartialChange) throw Exception()
        return when (change) {
            is LRPartialChange.LoadingStarted -> viewState.copy(loading = true, loadingError = null, canRefresh = false)
            is LRPartialChange.LoadingError -> viewState.copy(loading = false, loadingError = change.t)
            is LRPartialChange.RefreshStarted -> viewState.copy(refreshing = true, refreshingError = null)
            is LRPartialChange.RefreshError -> {
                viewActions.onNext(LRViewAction.RefreshingError(change.t))
                viewState.copy(refreshing = false, refreshingError = change.t)
            }
            is LRPartialChange.RefreshCancelled -> viewState.copy(refreshing = false)
            is LRPartialChange.InitialModelLoaded<*> -> {
                @Suppress("UNCHECKED_CAST")
                viewState.copy(
                        loading = false,
                        loadingError = null,
                        model = viewState.model.changeInitialModel(change.i as I) as M,
                        canRefresh = true,
                        refreshing = false,
                        refreshingError = null
                )
            }
        }
    }
}