package com.qwert2603.spenddemo.model.sync_processor

interface LocalDataSource<L : LocalItem, T : IdentifiableString> {
    fun saveItems(ts: List<L>)
    fun locallyDeleteItems(itemsIds: List<ItemsIds>)
    fun getLocallyChangedItems(count: Int): List<L>
    fun clearLocalChange(itemsIds: List<ItemsIds>)
    fun saveChangesFromRemote(updatesFromRemote: UpdatesFromRemote<T>)
    fun deleteItems(uuids: List<String>)
    fun deleteAll()
}