package com.qwert2603.spend.navigation

import android.support.v4.app.Fragment

interface NavigationActivity {
    fun onFragmentResumed(fragment: Fragment)
    fun onFragmentPaused(fragment: Fragment)
}