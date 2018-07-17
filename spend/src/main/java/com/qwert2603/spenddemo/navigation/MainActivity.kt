package com.qwert2603.spenddemo.navigation

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.qwert2603.andrlib.base.mvi.BaseFragment
import com.qwert2603.andrlib.util.toPx
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.AppInfoDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationActivity, KeyboardManager {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    var resumedFragment: Fragment? = null

    private val navigator = Navigator(object : ActivityInterface {
        override val supportFragmentManager = this@MainActivity.supportFragmentManager
        override val fragmentContainer = R.id.fragment_container
        override fun finish() = this@MainActivity.finish()
        override fun hideKeyboard() = this@MainActivity.hideKeyboard()
        override fun viewForSnackbars(): View = (resumedFragment as? BaseFragment<*, *, *>)?.viewForSnackbar()
                ?: activity_root_FrameLayout

        override val navigationActivity: NavigationActivity = this@MainActivity
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIHolder.diManager.viewsComponent.inject(this)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            router.newRootScreen(ScreenKey.RECORDS_LIST_MVVM.name)
        }

        if (savedInstanceState == null && intent.action == Intent.ACTION_VIEW) {
            AppInfoDialogFragment().show(supportFragmentManager, "about")
        }

        lifecycle.addObserver(navigatorHolder.createLifecycleObserver(navigator))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == "android.intent.action.VIEW") {
            AppInfoDialogFragment().show(supportFragmentManager, "about")
        }
    }

    override fun onBackPressed() {
        if ((resumedFragment as? BackPressListener)?.onBackPressed() == true) {
            return
        }
        router.exit()
    }

    override fun onFragmentResumed(fragment: Fragment) {
        if (fragment !in supportFragmentManager.fragments) return

        val isRoot = supportFragmentManager.backStackEntryCount == 0

        fragment.view
                ?.findViewById<Toolbar>(R.id.toolbar)
                ?.apply {
                    setSupportActionBar(this)
                    navigationIcon = if (isRoot) null else ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_arrow_back_white_24dp)
                    setNavigationOnClickListener { router.exit() }
                }
        resumedFragment = fragment
    }

    override fun onFragmentPaused(fragment: Fragment) {
        if (resumedFragment === fragment) resumedFragment = null
        fragment.view
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

    override fun isKeyBoardShown(): Boolean {
        return activity_root_FrameLayout.height < resources.displayMetrics.heightPixels - resources.toPx(30)
    }
}
