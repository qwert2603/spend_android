package com.qwert2603.spend.utils

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.qwert2603.andrlib.util.LogUtils

class LoggingFragmentLifecycleCallback : FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
        LogUtils.d("LoggingFragmentLifecycleCallback onFragmentViewCreated $f")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        LogUtils.d("LoggingFragmentLifecycleCallback onFragmentViewDestroyed $f")
    }

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        LogUtils.d("LoggingFragmentLifecycleCallback onFragmentAttached $f")
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        LogUtils.d("LoggingFragmentLifecycleCallback onFragmentDetached $f")
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        LogUtils.d("LoggingFragmentLifecycleCallback onFragmentCreated $f")
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        LogUtils.d("LoggingFragmentLifecycleCallback onFragmentDestroyed $f")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        LogUtils.d("LoggingFragmentLifecycleCallback onFragmentResumed $f")
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        LogUtils.d("LoggingFragmentLifecycleCallback onFragmentPaused $f")
    }
}