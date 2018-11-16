package com.qwert2603.spenddemo.model.sync_processor

import com.qwert2603.spenddemo.model.entity.LastUpdateInfo

interface RemoteDataSource<T : IdentifiableString> {
    fun getUpdates(lastUpdateInfo: LastUpdateInfo?, count: Int): UpdatesFromRemote<T>
    fun saveChanges(updated: List<T>, deletedUuids: List<String>)
}