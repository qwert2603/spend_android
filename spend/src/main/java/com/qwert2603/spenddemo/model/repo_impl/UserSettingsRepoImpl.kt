package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.PrefsDelegates
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepoImpl @Inject constructor(appContext: Context) : UserSettingsRepo {
    private val prefs = appContext.getSharedPreferences("user_settings.prefs", Context.MODE_PRIVATE)

    override var showIds by PrefsDelegates(prefs, "showIds")
    override var showChangeKinds by PrefsDelegates(prefs, "showChangeKinds")
    override var showChangeDateSums by PrefsDelegates(prefs, "showChangeDateSums")
    override var showChangeSpends by PrefsDelegates(prefs, "showChangeSpends")
    override var showChangeProfits by PrefsDelegates(prefs, "showChangeProfits")
}
