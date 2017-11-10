package com.qwert2603.spenddemo.base_mvi

import android.support.annotation.CallSuper
import com.qwert2603.spenddemo.navigation.MviBackStackFragment
import com.qwert2603.spenddemo.utils.LogUtils

abstract class BaseFragment<VS : Any, V : BaseView<VS>, P : BasePresenter<V, VS>> : MviBackStackFragment<V, P>(), BaseView<VS> {

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