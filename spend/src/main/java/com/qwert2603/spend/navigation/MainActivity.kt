package com.qwert2603.spend.navigation

import android.app.Service
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.drawable
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spend.NavGraphDirections
import com.qwert2603.spend.R
import com.qwert2603.spend.model.sync_processor.IsShowingToUserHolder
import com.qwert2603.spend.records_list.RecordsListFragmentArgs
import com.qwert2603.spend.records_list.RecordsListKey
import com.qwert2603.spend.utils.sameIn
import com.qwert2603.spend.utils.subscribeWhileResumed
import com.qwert2603.spend.utils.toMap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header_navigation.view.*
import org.koin.android.ext.android.get

class MainActivity : AppCompatActivity(), KeyboardManager {

    private val navigationAdapter = NavigationAdapter()

    private lateinit var headerNavigation: View

    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.navHost_FrameLayout) as NavHostFragment }
    private val navController by lazy { navHostFragment.findNavController() }

    private val rootNavigationItems = listOf(
            NavigationItem(R.drawable.icon, R.string.drawer_records, NavGraphDirections.actionGlobalRecordsListFragment(RecordsListKey.Now()), R.id.recordsListFragment),
            NavigationItem(R.drawable.ic_summa, R.string.drawer_sums, NavGraphDirections.actionGlobalSumsFragment(), R.id.sumsFragment),
            NavigationItem(R.drawable.ic_info_outline_black_24dp, R.string.drawer_about, NavGraphDirections.actionGlobalAboutFragment(), R.id.aboutFragment)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        get<IsShowingToUserHolder>().onActivityCreated(this)

        if (savedInstanceState == null) {
            LogUtils.d("MainActivity onCreate intent.action=${intent.action}")
        }

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val navHostFragment = NavHostFragment.create(
                    R.navigation.nav_graph,
                    RecordsListFragmentArgs(RecordsListKey.Now()).toBundle()
            )
            supportFragmentManager.beginTransaction()
                    .replace(R.id.navHost_FrameLayout, navHostFragment)
                    .setPrimaryNavigationFragment(navHostFragment)
                    .commitNow()
        }

        activity_DrawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerStateChanged(newState: Int) {
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })

        headerNavigation = navigation_view.inflate(R.layout.header_navigation)
        navigation_view.addHeaderView(headerNavigation)
        navigationAdapter.adapterList = BaseRecyclerViewAdapter.AdapterList(rootNavigationItems)
        headerNavigation.navigation_recyclerView.adapter = navigationAdapter
        headerNavigation.navigation_recyclerView.itemAnimator = null

        navigationAdapter.modelItemClicks
                .doOnNext { navigateToItem(it, true) }
                .subscribeWhileResumed(this)

        navigationAdapter.modelItemLongClicks
                .doOnNext { navigateToItem(it, false) }
                .subscribeWhileResumed(this)

        navHostFragment.childFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) {
                if (fragment is DialogFragment) return

                val isRoot = navHostFragment.childFragmentManager.backStackEntryCount == 0
                if (isRoot) {
                    val currentDestination = navController.currentDestination
                    navigationAdapter.selectedItemId = rootNavigationItems
                            .find {
                                it.destinationId == currentDestination?.id &&
                                        it.navDirections.arguments.toMap() sameIn fragment.arguments.toMap()
                            }
                            ?.id ?: 0
                } else {
                    navigationAdapter.selectedItemId = 0
                }

                fragment.view
                        ?.findViewById<Toolbar>(R.id.toolbar)
                        ?.apply {
                            setSupportActionBar(this)

                            navigationIcon = resources.drawable(when {
                                isRoot -> R.drawable.ic_menu_24dp
                                else -> R.drawable.ic_arrow_back_white_24dp
                            })
                            setNavigationOnClickListener {
                                if (isRoot) {
                                    hideKeyboard()
                                    activity_DrawerLayout.openDrawer(GravityCompat.START)
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        }
            }

            override fun onFragmentPaused(fm: FragmentManager, fragment: Fragment) {
                fragment.view
                        ?.findViewById<Toolbar>(R.id.toolbar)
                        ?.setNavigationOnClickListener(null)
            }
        }, false)
    }

    private fun navigateToItem(navigationItem: NavigationItem, newRootScreen: Boolean) {
        closeDrawer()
        val navOptions = if (newRootScreen) {
            NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .setEnterAnim(R.anim.nav_pop_enter)
                    .setExitAnim(R.anim.nav_exit)
                    .setPopEnterAnim(R.anim.nav_pop_enter)
                    .setPopExitAnim(R.anim.nav_pop_exit)
                    .build()
        } else {
            NavOptions.Builder()
                    .setEnterAnim(R.anim.nav_enter)
                    .setExitAnim(R.anim.nav_exit)
                    .setPopEnterAnim(R.anim.nav_pop_enter)
                    .setPopExitAnim(R.anim.nav_pop_exit)
                    .build()
        }
        navController.navigate(navigationItem.navDirections, navOptions)
    }

    override fun onBackPressed() {
        if (closeDrawer()) return
        val fragment = navHostFragment.childFragmentManager.fragments.find { it.isVisible }
        if ((fragment as? BackPressListener)?.onBackPressed() == true) {
            return
        }
        super.onBackPressed()
    }

    private fun closeDrawer(): Boolean = activity_DrawerLayout.isDrawerOpen(GravityCompat.START)
            .also { if (it) activity_DrawerLayout.closeDrawer(GravityCompat.START) }

    override fun hideKeyboard(removeFocus: Boolean) {
        if (removeFocus) {
            activity_root_FrameLayout.requestFocus()
        }
        val currentFocus = currentFocus ?: return
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }

    override fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        (getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
    }
}
