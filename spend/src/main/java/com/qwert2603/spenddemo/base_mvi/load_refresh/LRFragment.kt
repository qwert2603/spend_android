package com.qwert2603.spenddemo.base_mvi.load_refresh

import android.support.annotation.CallSuper
import android.support.design.widget.Snackbar
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.base_mvi.BaseFragment
import com.qwert2603.spenddemo.base_mvi.BasePresenter
import com.qwert2603.spenddemo.base_mvi.BaseView
import com.qwert2603.spenddemo.base_mvi.ViewAction
import com.qwert2603.spenddemo.utils.LogUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class LRFragment<M : InitialModelHolder<*>, V : BaseView<LRViewState<M>>, P : BasePresenter<V, LRViewState<M>>> : BaseFragment<LRViewState<M>, V, P>(), LRView<M> {

    protected abstract fun loadRefreshPanel(): LoadRefreshPanel

    protected val retryRefreshSubject: PublishSubject<Any> = PublishSubject.create<Any>()

    override fun load(): Observable<Any> = Observable.just(Any())

    override fun retry(): Observable<Any> = loadRefreshPanel().retryClicks()

    override fun refresh(): Observable<Any> = Observable.merge(
            loadRefreshPanel().refreshes(),
            retryRefreshSubject
    )

    @CallSuper override fun render(vs: LRViewState<M>) {
        super.render(vs)
        vs.loadingError?.let { LogUtils.e("loadingError", it) }
        loadRefreshPanel().render(vs)
    }

    @CallSuper override fun executeAction(va: ViewAction) {
        LogUtils.d { "${this.javaClass.simpleName} executeAction $va" }
        if (va is LRViewAction) {
            when (va) {
                is LRViewAction.RefreshingError -> {
                    LogUtils.e("refreshError", va.t)
                    viewForSnackbar()?.let {
                        Snackbar.make(it, R.string.refreshing_error_text, Snackbar.LENGTH_SHORT)
                                .setAction(R.string.retry_text, { retryRefreshSubject.onNext(Any()) })
                                .show()
                    }
                }
            }
        }
    }

    protected fun isLoadingFinishedNow() = prevViewState?.isModelLoaded == false && currentViewState.isModelLoaded
    protected fun isRefreshingFinishedNow() = prevViewState?.refreshing == true && !currentViewState.refreshing && currentViewState.refreshingError == null
    protected fun isInitialModelLoadedOrUpdatedNow() = isLoadingFinishedNow() || isRefreshingFinishedNow()
}