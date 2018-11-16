package com.qwert2603.spenddemo.model.sync_processor

import android.content.SharedPreferences
import com.google.gson.Gson
import com.qwert2603.spenddemo.model.entity.LastUpdateInfo
import com.qwert2603.spenddemo.utils.PreferenceUtils

interface LastUpdateStorage {
    var lastUpdateInfo: LastUpdateInfo?
}

class PrefsLastUpdateStorage(prefs: SharedPreferences, gson: Gson) : LastUpdateStorage {
    override var lastUpdateInfo: LastUpdateInfo? by PreferenceUtils.createPrefsObjectNullable(prefs, "lastUpdateInfo", gson)
}