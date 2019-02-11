package com.qwert2603.spend.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder

const val SCREEN_KEY_KEY = "SCREEN_KEY_KEY"

fun Fragment.setScreen(mfcScreen: SpendScreen) {
    val args: Bundle = arguments ?: Bundle()
    args.putSerializable(SCREEN_KEY_KEY, mfcScreen)
    arguments = args
}

fun Fragment.getScreen(): SpendScreen? = arguments?.getSerializable(SCREEN_KEY_KEY) as? SpendScreen

fun NavigatorHolder.createLifecycleObserver(navigator: Navigator) = object : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        setNavigator(navigator)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        removeNavigator()
    }
}