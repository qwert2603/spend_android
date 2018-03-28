package com.qwert2603.spenddemo.navigation

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.ViewGroupCompat
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.fragmentargs.FragmentArgs
import com.qwert2603.andrlib.base.mvi.BaseFragment
import com.qwert2603.spenddemo.changes_list.ChangesListFragment
import com.qwert2603.spenddemo.records_list.RecordsListFragment
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

class Navigator(private val activity: ActivityInterface)
    : SupportFragmentNavigator(activity.supportFragmentManager, activity.fragmentContainer) {

    init {
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentResumed(fm: FragmentManager?, f: Fragment) {
                        activity.navigationActivity.onFragmentResumed(f)
                    }

                    override fun onFragmentPaused(fm: FragmentManager?, f: Fragment) {
                        activity.navigationActivity.onFragmentPaused(f)
                    }
                }, false
        )

        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentPreCreated(fm: FragmentManager?, f: Fragment, savedInstanceState: Bundle?) {
                        FragmentArgs.inject(f)
                    }

                    override fun onFragmentViewCreated(fm: FragmentManager?, f: Fragment?, v: View?, savedInstanceState: Bundle?) {
                        (v as? ViewGroup)?.let { ViewGroupCompat.setTransitionGroup(it, true) }
                    }
                }, true
        )
    }

    override fun createFragment(screenKey: String, data: Any?) = when (ScreenKey.valueOf(screenKey)) {
        ScreenKey.RECORDS_LIST -> RecordsListFragment()
        ScreenKey.CHANGES_LIST -> ChangesListFragment()
    }.also { it.setScreenKey(ScreenKey.valueOf(screenKey)) }

    override fun exit() {
        activity.finish()
    }

    override fun showSystemMessage(message: String) {
        /** make delay to wait for new fragment to show snackbar on its [BaseFragment.viewForSnackbar] */
        activity.viewForSnackbars().postDelayed({
            Snackbar.make(activity.viewForSnackbars(), message, Snackbar.LENGTH_SHORT).show()
        }, 300)
    }

    override fun setupFragmentTransactionAnimation(command: Command, currentFragment: Fragment?, nextFragment: Fragment, fragmentTransaction: FragmentTransaction) {
        currentFragment?.exitTransition = Slide(Gravity.START)
                .also { it.duration = 230 }
        nextFragment.enterTransition = Slide(if (command is Forward) Gravity.END else Gravity.START)
                .also { it.duration = 230 }
    }

    override fun applyCommand(command: Command?) {
        activity.hideKeyboard()
        super.applyCommand(command)
    }
}