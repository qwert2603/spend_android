package com.qwert2603.spenddemo.navigation

import android.app.Service
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.records_list.RecordsListFragment
import kotlinx.android.synthetic.main.activity_main.*
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationActivity, KeyboardManager {

    @Inject lateinit var router: Router
    @Inject lateinit var navigatorHolder: NavigatorHolder

    var resumedFragment: MviBackStackFragment<*, *>? = null

    private val navigator = Navigator(object : ActivityInterface {
        override val supportFragmentManager = this@MainActivity.supportFragmentManager
        override val fragmentContainer = R.id.fragment_container
        override fun finish() = this@MainActivity.finish()
        override fun hideKeyboard() = this@MainActivity.hideKeyboard()
        override fun viewForSnackbars(): View = resumedFragment?.viewForSnackbars() ?: activity_root_FrameLayout
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIHolder.diManager.viewsComponent.inject(this)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            router.newRootScreen(RecordsListFragment.TAG)
        }
    }


    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed() {
        router.exit()
    }

    override fun onFragmentResumed(backStackFragment: MviBackStackFragment<*, *>) {
        val isRoot = supportFragmentManager.backStackEntryCount == 0

        backStackFragment.getView()
                ?.findViewById<Toolbar>(R.id.toolbar)
                ?.apply {
                    setSupportActionBar(this)
                    navigationIcon = if (isRoot) null else ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_arrow_back_white_24dp)
                    setNavigationOnClickListener {
                        router.exit()
                    }
                }
        resumedFragment = backStackFragment
    }

    override fun onFragmentPaused(backStackFragment: MviBackStackFragment<*, *>) {
        if (resumedFragment === backStackFragment) resumedFragment = null
        backStackFragment.getView()
                ?.findViewById<Toolbar>(R.id.toolbar)
                ?.setNavigationOnClickListener(null)
    }

    override fun hideKeyboard(removeFocus: Boolean) {
        if (removeFocus) {
            activity_root_FrameLayout.requestFocus()
        }
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }

    override fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        (getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
    }
}
