package com.qwert2603.spend.model.sync_processor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.SpendDemoApplication

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            LogUtils.d("BootCompleteReceiver onReceive")
            SpendDemoApplication.debugHolder.logLine { "BootCompleteReceiver onReceive" }
            SyncWorkReceiver.scheduleNext(context.applicationContext)
        }
    }
}