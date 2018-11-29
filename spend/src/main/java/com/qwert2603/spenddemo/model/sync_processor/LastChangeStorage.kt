package com.qwert2603.spenddemo.model.sync_processor

import android.content.SharedPreferences
import com.google.gson.Gson
import com.qwert2603.spenddemo.model.rest.entity.LastChangeInfo
import com.qwert2603.spenddemo.utils.PreferenceUtils

interface LastChangeStorage {
    var lastChangeInfo: LastChangeInfo?
}

class PrefsLastChangeStorage(prefs: SharedPreferences, gson: Gson) : LastChangeStorage {
    override var lastChangeInfo: LastChangeInfo? by PreferenceUtils.createPrefsObjectNullable(prefs, "lastChangeInfo", gson)
}