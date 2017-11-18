package com.qwert2603.spenddemo.base_mvi

import android.support.annotation.CallSuper
import android.view.View
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.qwert2603.spenddemo.utils.LogUtils

abstract class BaseFragment<VS : Any, V : BaseView<VS>, P : BasePresenter<V, VS>> : MviFragment<V, P>(), BaseView<VS> {

    open fun viewForSnackbar(): View? = view

    private var everRendered = false
    protected var prevViewState: VS? = null
    protected lateinit var currentViewState: VS

    @CallSuper override fun render(vs: VS) {
        if (everRendered) {
            prevViewState = currentViewState
        } else {
            everRendered = true
        }
        currentViewState = vs
        LogUtils.d { "${this.javaClass.simpleName} render $vs" }
    }
}