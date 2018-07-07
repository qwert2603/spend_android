package com.qwert2603.spenddemo.model.sync_processor

import java.sql.Timestamp

interface RemoteItem {
    val id: Long
    val updated: Timestamp
    val deleted: Boolean
}