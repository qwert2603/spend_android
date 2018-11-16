package com.qwert2603.spenddemo.navigation

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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

    private val navigator = Navigator(this, R.id.fragment_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIHolder.diManager.viewsComponent.inject(this)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            router.newRootScreen(SpendScreen.RecordsList)
        }

        if (savedInstanceState == null && intent.action == Intent.ACTION_VIEW) {
            AppInfoDialogFragment().show(supportFragmentManager, "about")
        }

        lifecycle.addObserver(navigatorHolder.createLifecycleObserver(navigator))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == "android.intent.action.VIEW") {
            AppInfoDialogFragment().show(supportFragmentManager, null)
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if ((fragment as? BackPressListener)?.onBackPressed() == true) {
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
    }

    override fun onFragmentPaused(fragment: Fragment) {
        fragment.view
                ?.findViewById<Toolbar>(R.id.toolbar)
                ?.setNavigationOnClickListener(null)
    }

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

    override fun isKeyBoardShown(): Boolean {
        // todo: don't work in screen splitting
        return activity_root_FrameLayout.height < resources.displayMetrics.heightPixels - resources.toPx(30)
    }
}
