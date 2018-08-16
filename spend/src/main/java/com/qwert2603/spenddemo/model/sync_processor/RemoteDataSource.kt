package com.qwert2603.spenddemo.model.sync_processor

import com.qwert2603.andrlib.model.IdentifiableLong
import java.sql.Timestamp

// todo: send changes in groups.
interface RemoteDataSource<T : IdentifiableLong, R : RemoteItem> {
    fun getUpdates(lastUpdateMillis: Timestamp, lastUpdatedId: Long, count: Int): List<R>
    fun addItem(t: T): Long
    fun editItem(t: T)
    fun deleteItem(id: Long)
}