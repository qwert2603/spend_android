package com.qwert2603.spend.model.rest.entity

data class GetRecordsUpdatesResult(
        val updatedCategories: List<RecordCategoryServer>,
        val updatedRecords: List<RecordServer>,
        val deletedRecordsUuid: List<String>,
        val lastChangeInfo: LastChangeInfo
) {
    fun isEmpty() = updatedCategories.isEmpty() && updatedRecords.isEmpty() && deletedRecordsUuid.isEmpty()
}