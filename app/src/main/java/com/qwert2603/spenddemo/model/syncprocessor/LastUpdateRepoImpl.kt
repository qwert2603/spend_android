package com.qwert2603.spenddemo.model.syncprocessor

import android.content.Context
import android.preference.PreferenceManager
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.utils.LogUtils
import com.qwert2603.syncprocessor.datasource.LastUpdateRepo
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LastUpdateRepoImpl @Inject constructor(
        appContext: Context
) : LastUpdateRepo {

    companion object {
        private const val LAST_UPDATE_KEY = "${BuildConfig.APPLICATION_ID}.LAST_UPDATE_KEY"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)

    override fun getLastUpdate(): Long = prefs.getLong(LAST_UPDATE_KEY, 0L)

    override fun saveLastUpdate(millis: Long) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS", Locale.getDefault())
        LogUtils.d("LastUpdateRepoImpl saveLastUpdate ${dateFormat.format(Date(millis))}")
        prefs.edit()
                .putLong(LAST_UPDATE_KEY, millis)
                .apply()
    }
}