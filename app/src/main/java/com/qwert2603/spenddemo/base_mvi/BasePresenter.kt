package com.qwert2603.spenddemo.base_mvi

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.utils.addTo
import com.qwert2603.spenddemo.utils.pausable
import com.qwert2603.spenddemo.utils.switchToUiIfNotYet
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

abstract class BasePresenter<V : BaseView<VS>, VS>(protected val uiSchedulerProvider: UiSchedulerProvider) : MviBasePresenter<V, VS>() {

    private val viewAttached = PublishSubject.create<Boolean>()

    protected val viewActions: PublishSubject<ViewAction> = PublishSubject.create<ViewAction>()
    private val actionsRelay = viewActions.pausable(viewAttached)
    private val actionsObservable: PublishSubject<ViewAction> = PublishSubject.create<ViewAction>()

    private var relayDisposable: Disposable? = null
    protected val actionsDisposable = CompositeDisposable()

    private val disposableView = CompositeDisposable()

    override fun attachView(view: V) {
        super.attachView(view)
        actionsObservable
                .switchToUiIfNotYet(uiSchedulerProvider)
                .subscribe(view::executeAction)
                .addTo(actionsDisposable)
        if (relayDisposable == null) {
            relayDisposable = actionsRelay.subscribe { actionsObservable.onNext(it) }
        }
        viewAttached.onNext(true)
    }

    override fun detachView(retainInstance: Boolean) {
        super.detachView(retainInstance)
        viewAttached.onNext(false)
        actionsDisposable.clear()
        if (!retainInstance) {
            relayDisposable?.dispose()
            disposableView.dispose()
        }
    }

    protected fun <T> Observable<T>.subscribeToView() = this
            .subscribe()
            .addTo(disposableView)
}