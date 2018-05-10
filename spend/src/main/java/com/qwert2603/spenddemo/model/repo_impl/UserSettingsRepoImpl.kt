package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.PrefsBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepoImpl @Inject constructor(appContext: Context) : UserSettingsRepo {
    private val prefs = appContext.getSharedPreferences("user_settings.prefs", Context.MODE_PRIVATE)

    override var showIds by PrefsBoolean(prefs, "showIds")
    override var showChangeKinds by PrefsBoolean(prefs, "showChangeKinds")
    override var showDateSums by PrefsBoolean(prefs, "showDateSums")
    override var showSpends by PrefsBoolean(prefs, "showSpends", true)
    override var showProfits by PrefsBoolean(prefs, "showProfits", true)
}