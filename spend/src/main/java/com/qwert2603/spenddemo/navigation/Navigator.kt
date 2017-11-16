package com.qwert2603.spenddemo.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.transition.Slide
import android.view.Gravity
import com.qwert2603.spenddemo.changes_list.ChangesListFragment
import com.qwert2603.spenddemo.records_list.RecordsListFragment
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

class Navigator(private val activity: ActivityInterface)
    : SupportFragmentNavigator(activity.supportFragmentManager, activity.fragmentContainer) {

    override fun createFragment(screenKey: String, data: Any?) = when (screenKey) {
        RecordsListFragment.TAG -> RecordsListFragment()
        ChangesListFragment.TAG -> ChangesListFragment()
        else -> null!!
    }.let { it as Fragment }.also {
        val args: Bundle = it.arguments ?: Bundle()
        args.putString(MviBackStackFragment.SCREEN_KEY_KEY, screenKey)
        it.arguments = args
    }

    override fun exit() {
        activity.finish()
    }

    override fun showSystemMessage(message: String) {
        /** make delay to wait for new fragment to show snackbar on its [MviBackStackFragment.viewForSnackbars] */
        activity.viewForSnackbars().postDelayed({
            Snackbar.make(activity.viewForSnackbars(), message, Snackbar.LENGTH_SHORT).show()
        }, 300)
    }

    @SuppressLint("RtlHardcoded")
    override fun setupFragmentTransactionAnimation(command: Command, currentFragment: Fragment?, nextFragment: Fragment, fragmentTransaction: FragmentTransaction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            currentFragment?.exitTransition = Slide(Gravity.LEFT)
                    .also { it.duration = 230 }
            nextFragment.enterTransition = Slide(if (command is Forward) Gravity.RIGHT else Gravity.LEFT)
                    .also { it.duration = 230 }
        }
    }

    override fun applyCommand(command: Command?) {
        activity.hideKeyboard()
        super.applyCommand(command)
    }
}