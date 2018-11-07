package com.qwert2603.spenddemo.navigation

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.ViewGroupCompat
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.fragmentargs.FragmentArgs
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

class Navigator<A>(private val activity: A, fragmentContainer: Int)
    : SupportAppNavigator(activity, fragmentContainer)
        where A : FragmentActivity,
              A : NavigationActivity,
              A : KeyboardManager {

    init {
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                        // todo: set transitions
                    }

                    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                        activity.onFragmentResumed(f)
                    }

                    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
                        activity.onFragmentPaused(f)
                    }
                }, false
        )

        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentPreCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                        FragmentArgs.inject(f)
                    }

                    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                        (v as? ViewGroup)?.let { ViewGroupCompat.setTransitionGroup(it, true) }
                    }
                }, true
        )
    }

    override fun setupFragmentTransaction(command: Command?, currentFragment: Fragment?, nextFragment: Fragment?, fragmentTransaction: FragmentTransaction?) {
        currentFragment?.exitTransition = Slide(Gravity.START)
                .also { it.duration = 230 }
        nextFragment?.enterTransition = Slide(if (command is Forward) Gravity.END else Gravity.START)
                .also { it.duration = 230 }
    }

    override fun applyCommand(command: Command?) {
        activity.hideKeyboard()
        super.applyCommand(command)
    }
}