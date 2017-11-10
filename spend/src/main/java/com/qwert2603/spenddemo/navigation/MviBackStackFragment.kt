package com.qwert2603.spenddemo.navigation

import android.os.Bundle
import android.support.v4.view.ViewGroupCompat
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.fragmentargs.FragmentArgs
import com.hannesdorfmann.mosby3.mvi.BuildConfig
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView

abstract class MviBackStackFragment<V : MvpView, P : MviPresenter<V, *>> : MviFragment<V, P>() {

    companion object {
        const val SCREEN_KEY_KEY = BuildConfig.APPLICATION_ID + ".SCREEN_KEY_KEY"
    }

    fun screenKey(): String? = arguments?.getString(SCREEN_KEY_KEY)
    open fun viewForSnackbars(): View? = view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewGroupCompat.setTransitionGroup(view as ViewGroup, true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        // todo: use lifecycle
        (activity as? NavigationActivity)?.onFragmentResumed(this)
    }

    override fun onPause() {
        super.onPause()
        (activity as? NavigationActivity)?.onFragmentPaused(this)
    }
}