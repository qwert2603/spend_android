package com.qwert2603.spenddemo.model.sync_processor

import com.qwert2603.andrlib.model.IdentifiableLong

interface LocalDataSource<T : IdentifiableLong, L : LocalItem> {
    fun saveItem(t: L)
    fun addItems(ts: List<L>)
    fun deleteItem(id: Long)
    fun clearLocalChange(itemId: Long, changeId: Long)
    fun getLocallyChangedItems(count: Int): List<L>
    fun locallyDeleteItem(itemId: Long, changeId: Long)
    fun clearAll()
    fun onItemAddedToServer(localId: Long, newId: Long, changeId: Long)
    fun saveChangeFromServer(t: T)
    fun onItemEdited(t: T, changeId: Long)
}