package com.qwert2603.spenddemo.model.sync_processor

import com.qwert2603.spenddemo.model.entity.LastUpdateInfo

data class UpdatesFromRemote<T : IdentifiableString>(
        val updatedItems: List<T>,
        val deletedItemsUuid: List<String>,
        val lastUpdateInfo: LastUpdateInfo
) {
    fun isEmpty() = updatedItems.isEmpty() && deletedItemsUuid.isEmpty()
}