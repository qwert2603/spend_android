package com.qwert2603.spenddemo.utils

import android.content.Context
import android.os.Looper
import com.qwert2603.spenddemo.env.E
import java.text.SimpleDateFormat
import java.util.*

class DebugHolder(appContext: Context) {

    var log by PrefsString(
            prefs = appContext.getSharedPreferences("debug_holder.prefs", Context.MODE_PRIVATE),
            key = "log"
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun logLine(msg: () -> String) {
        if (E.env.buildForTesting()) {
            synchronized(this) {
                log += "${dateFormat.format(Date())} ${Looper.myLooper() == Looper.getMainLooper()} ${msg()}\n"
            }
        }
    }

    fun clearLog() {
        if (E.env.buildForTesting()) {
            synchronized(this) {
                log = ""
            }
        }
    }
}