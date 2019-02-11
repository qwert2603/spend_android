package com.qwert2603.spend.navigation

import androidx.fragment.app.Fragment

interface NavigationActivity {
    fun onFragmentResumed(fragment: Fragment)
    fun onFragmentPaused(fragment: Fragment)
}