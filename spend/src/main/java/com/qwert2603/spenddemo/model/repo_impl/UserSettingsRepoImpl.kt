package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.google.gson.Gson
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.PreferenceUtils
import com.qwert2603.spenddemo.utils.PrefsBoolean
import com.qwert2603.spenddemo.utils.PrefsInt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepoImpl @Inject constructor(appContext: Context) : UserSettingsRepo {

    private val prefs = appContext.getSharedPreferences("user_settings.prefs", Context.MODE_PRIVATE)

    override var showSpends by PrefsBoolean(prefs, "showSpends", true)
    override var showProfits by PrefsBoolean(prefs, "showProfits", true)
    override var showSums by PrefsBoolean(prefs, "showSums")
    override var showChangeKinds by PrefsBoolean(prefs, "showChangeKinds")
    override var showTimes by PrefsBoolean(prefs, "showTimes", true)

    override var longSumPeriodDays by PrefsInt(prefs, "longSumPeriodDays", 30)
    override var shortSumPeriodMinutes by PrefsInt(prefs, "shortSumPeriodMinutes", 5)

    override val fixedTime: Pair<SDate, STime?>? by PreferenceUtils.createPrefsObjectNullable(prefs,"fixedTime", Gson())
}
