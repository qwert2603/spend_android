package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.google.gson.Gson
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.ObservableField
import com.qwert2603.spenddemo.utils.PreferenceUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepoImpl @Inject constructor(appContext: Context) : UserSettingsRepo {

    private val prefs = appContext.getSharedPreferences("user_settings.prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    override val longSumPeriod: ObservableField<Days> = PreferenceUtils.createPrefsObjectObservable(prefs, "longSumPeriod", gson, 30.days)
    override val shortSumPeriod: ObservableField<Minutes> = PreferenceUtils.createPrefsObjectObservable(prefs, "shortSumPeriod", gson, 5.minutes)
    override val showInfo: ObservableField<ShowInfo> = PreferenceUtils.createPrefsObjectObservable(prefs, "showInfo", gson, ShowInfo.DEFAULT)
    override val sumsShowInfo: ObservableField<SumsShowInfo> = PreferenceUtils.createPrefsObjectObservable(prefs, "sumsShowInfo", gson, SumsShowInfo.DEFAULT)
}