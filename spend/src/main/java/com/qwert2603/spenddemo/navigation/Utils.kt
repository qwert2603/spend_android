package com.qwert2603.spenddemo.navigation

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.qwert2603.spenddemo.BuildConfig
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder

const val SCREEN_KEY_KEY = BuildConfig.APPLICATION_ID + ".SCREEN_KEY_KEY"

fun Fragment.setScreenKey(screenKey: ScreenKey) {
    val args: Bundle = arguments ?: Bundle()
    args.putString(SCREEN_KEY_KEY, screenKey.name)
    arguments = args
}

fun Fragment.getScreenKey(): ScreenKey? = arguments
        ?.getString(SCREEN_KEY_KEY, null)
        ?.let { ScreenKey.valueOf(it) }

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