package com.qwert2603.spenddemo.model.syncprocessor

import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.syncprocessor.entity.Identifiable
import java.util.*

data class SyncingSpend(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date
) : Identifiable<Long>

fun SyncingSpend.toSpend() = Spend(id, kind, value, date)
fun Spend.toSyncingSpend() = SyncingSpend(id, kind, value, date)