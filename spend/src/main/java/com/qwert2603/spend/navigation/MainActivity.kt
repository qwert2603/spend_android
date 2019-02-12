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
import androidx.fragment.app.Fragment
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.drawable
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spend.R
import com.qwert2603.spend.di.DIHolder
import com.qwert2603.spend.model.sync_processor.IsShowingToUserHolder
import com.qwert2603.spend.records_list.RecordsListKey
import com.qwert2603.spend.utils.subscribeWhileResumed
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
    @Inject
    lateinit var isShowingToUserHolder: IsShowingToUserHolder

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
            NavigationItem(R.drawable.icon, R.string.drawer_records, SpendScreen.RecordsList(RecordsListKey.Now())),
            NavigationItem(R.drawable.ic_summa, R.string.drawer_sums, SpendScreen.Sums()),
            NavigationItem(R.drawable.ic_info_outline_black_24dp, R.string.drawer_about, SpendScreen.About())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIHolder.diManager.viewsComponent.inject(this)

        isShowingToUserHolder.onActivityCreated(this)

        if (savedInstanceState == null) {
            LogUtils.d("MainActivity onCreate intent.action=${intent.action}")
        }

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            router.newRootScreen(SpendScreen.RecordsList(RecordsListKey.Now()))
        }

        headerNavigation = navigation_view.inflate(R.layout.header_navigation)
        navigation_view.addHeaderView(headerNavigation)
        navigationAdapter.adapterList = BaseRecyclerViewAdapter.AdapterList(rootNavigationItems)
        headerNavigation.navigation_recyclerView.adapter = navigationAdapter
        headerNavigation.navigation_recyclerView.itemAnimator = null

        navigationAdapter.modelItemClicks
//                .doOnSubscribe { LogUtils.d("MainActivity navigationAdapter.modelItemClicks doOnSubscribe") }
//                .doOnNext { LogUtils.d("MainActivity navigationAdapter.modelItemClicks doOnNext") }
//                .doOnDispose { LogUtils.d("MainActivity navigationAdapter.modelItemClicks doOnDispose") }
                .doOnNext { navigateToItem(it, true) }
                .subscribeWhileResumed(this)

        navigationAdapter.modelItemLongClicks
                .doOnNext { navigateToItem(it, false) }
                .subscribeWhileResumed(this)

        lifecycle.addObserver(navigatorHolder.createLifecycleObserver(navigator))
    }


    override fun onStart() {
        super.onStart()
        activity_DrawerLayout.addDrawerListener(drawerListener)
    }

    override fun onStop() {
        activity_DrawerLayout.removeDrawerListener(drawerListener)
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
