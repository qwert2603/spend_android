package com.qwert2603.spenddemo.navigation

import android.os.Bundle
import android.support.v4.app.Fragment
import com.qwert2603.spenddemo.BuildConfig

const val SCREEN_KEY_KEY = BuildConfig.APPLICATION_ID + ".SCREEN_KEY_KEY"

fun Fragment.setScreenKey(screenKey: String) {
    val args: Bundle = arguments ?: Bundle()
    args.putString(SCREEN_KEY_KEY, screenKey)
    arguments = args
}

fun Fragment.getScreenKey(): String? = arguments?.getString(SCREEN_KEY_KEY, null)