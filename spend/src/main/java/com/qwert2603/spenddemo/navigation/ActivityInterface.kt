package com.qwert2603.spenddemo.navigation

import android.support.v4.app.FragmentManager
import android.view.View

interface ActivityInterface {
    val supportFragmentManager: FragmentManager
    val fragmentContainer: Int
    fun finish()
    fun hideKeyboard()
    fun viewForSnackbars(): View
}