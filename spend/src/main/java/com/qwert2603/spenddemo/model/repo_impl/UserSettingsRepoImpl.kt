package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import android.content.SharedPreferences
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.PrefsBoolean
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepoImpl @Inject constructor(appContext: Context) : UserSettingsRepo {

    companion object {
        private const val KEY_SHOW_TIMES = "showTimes"
    }

    private val prefs = appContext.getSharedPreferences("user_settings.prefs", Context.MODE_PRIVATE)

    override var showIds by PrefsBoolean(prefs, "showIds")
    override var showChangeKinds by PrefsBoolean(prefs, "showChangeKinds")
    override var showDateSums by PrefsBoolean(prefs, "showDateSums")
    override var showMonthSums by PrefsBoolean(prefs, "showMonthSums")
    override var showSpends by PrefsBoolean(prefs, "showSpends", true)
    override var showProfits by PrefsBoolean(prefs, "showProfits", true)
    override var showBalance by PrefsBoolean(prefs, "showBalance", true)
    override var showTimes by PrefsBoolean(prefs, KEY_SHOW_TIMES, true)

    private val showTimesChanges = BehaviorSubject.createDefault(showTimes)

    private val listener = object : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            LogUtils.d("UserSettingsRepoImpl registerOnSharedPreferenceChangeListener $key ${prefs.getBoolean(KEY_SHOW_TIMES, false)}")
            if (key == KEY_SHOW_TIMES) {
                showTimesChanges.onNext(prefs.getBoolean(KEY_SHOW_TIMES, false))
            }
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun showTimesChanges(): Observable<Boolean> = showTimesChanges
}
