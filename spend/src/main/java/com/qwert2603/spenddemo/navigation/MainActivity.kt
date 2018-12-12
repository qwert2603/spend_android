package com.qwert2603.spenddemo.navigation

import android.app.Service
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.addTo
import com.qwert2603.andrlib.util.drawable
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header_navigation.view.*
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationActivity, KeyboardManager {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    private val navigator = Navigator(this, R.id.fragment_container)

    private val navigationAdapter = NavigationAdapter()

    private lateinit var headerNavigation: View

    private val drawerListener = object : DrawerLayout.SimpleDrawerListener() {
        override fun onDrawerStateChanged(newState: Int) {
            if (newState == DrawerLayout.STATE_DRAGGING) {
                hideKeyboard()
            }
        }
    }

    private val rootNavigationItems = listOf(
            NavigationItem(R.drawable.icon, R.string.drawer_records, SpendScreen.RecordsList),
            NavigationItem(R.drawable.ic_summa, R.string.drawer_sums, SpendScreen.Sums),
            NavigationItem(R.drawable.ic_info_outline_black_24dp, R.string.drawer_about, SpendScreen.About)
    )

    private val navigationDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIHolder.diManager.viewsComponent.inject(this)

        LogUtils.d("MainActivity onCreate intent.action=${intent.action}")

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            router.newRootScreen(SpendScreen.RecordsList)
        }

        headerNavigation = navigation_view.inflate(R.layout.header_navigation)
        navigation_view.addHeaderView(headerNavigation)
        headerNavigation.navigation_recyclerView.itemAnimator = null

        lifecycle.addObserver(navigatorHolder.createLifecycleObserver(navigator))
    }


    override fun onStart() {
        super.onStart()

        navigationAdapter.modelItemClicks
                .subscribe { navigateToItem(it, true) }
                .addTo(navigationDisposable)

        navigationAdapter.modelItemLongClicks
                .subscribe { navigateToItem(it, false) }
                .addTo(navigationDisposable)

        headerNavigation.navigation_recyclerView.adapter = navigationAdapter

        if (navigationAdapter.adapterList.modelList.isEmpty()) {
            navigationAdapter.adapterList = BaseRecyclerViewAdapter.AdapterList(rootNavigationItems)
        }

        activity_DrawerLayout.addDrawerListener(drawerListener)
    }

    override fun onStop() {
        with(headerNavigation) {
            navigation_recyclerView.adapter = null
        }
        activity_DrawerLayout.removeDrawerListener(drawerListener)
        navigationDisposable.clear()
        super.onStop()
    }

    private fun navigateToItem(navigationItem: NavigationItem, newRootScreen: Boolean) {
        closeDrawer()
        if (newRootScreen) {
            router.newRootScreen(navigationItem.screen)
        } else {
            router.navigateTo(navigationItem.screen)
        }
    }

    override fun onBackPressed() {
        if (closeDrawer()) return
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if ((fragment as? BackPressListener)?.onBackPressed() == true) {
            return
        }
        router.exit()
    }

    override fun onFragmentResumed(fragment: Fragment) {
        if (fragment !in supportFragmentManager.fragments) return

        val screen: SpendScreen = fragment.getScreen() ?: return
        val isRoot = supportFragmentManager.backStackEntryCount == 0
        if (isRoot) {
            navigationAdapter.selectedItemId = rootNavigationItems.find { screen == it.screen }?.id ?: 0
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
                            router.exit()
                        }
                    }
                }
    }

    override fun onFragmentPaused(fragment: Fragment) {
        fragment.view
                ?.findViewById<Toolbar>(R.id.toolbar)
                ?.setNavigationOnClickListener(null)
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
