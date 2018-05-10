package com.qwert2603.spenddemo.model.syncprocessor

import com.qwert2603.syncprocessor.entity.Identifiable
import com.qwert2603.syncprocessor.entity.RemoteItem
import java.util.*

data class RemoteSpend(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        override val updated: Long,
        override val deleted: Boolean
) : RemoteItem, Identifiable<Long>

fun RemoteSpend.toSyncingSpend() = SyncingSpend(id, kind, value, date)