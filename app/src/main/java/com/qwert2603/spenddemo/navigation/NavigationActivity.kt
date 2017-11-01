package com.qwert2603.spenddemo.navigation

interface NavigationActivity {
    fun onFragmentResumed(backStackFragment: MviBackStackFragment<*, *>)
    fun onFragmentPaused(backStackFragment: MviBackStackFragment<*, *>)
}