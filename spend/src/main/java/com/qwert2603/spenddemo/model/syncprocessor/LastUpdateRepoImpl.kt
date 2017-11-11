package com.qwert2603.spenddemo.model.syncprocessor

import android.content.Context
import android.preference.PreferenceManager
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.utils.LogUtils
import com.qwert2603.syncprocessor.datasource.LastUpdateRepo
import java.text.SimpleDateFormat
import java.util.*

class LastUpdateRepoImpl(
        appContext: Context
) : LastUpdateRepo {

    companion object {
        private const val LAST_UPDATE_KEY = "${BuildConfig.APPLICATION_ID}.LAST_UPDATE_KEY"
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS", Locale.getDefault())
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)

    override fun getLastUpdate(): Long = prefs.getLong(LAST_UPDATE_KEY, 0L)

    override fun saveLastUpdate(millis: Long) {
        LogUtils.d("LastUpdateRepoImpl saveLastUpdate ${DATE_FORMAT.format(Date(millis))}")
        prefs.edit()
                .putLong(LAST_UPDATE_KEY, millis)
                .apply()
    }
}