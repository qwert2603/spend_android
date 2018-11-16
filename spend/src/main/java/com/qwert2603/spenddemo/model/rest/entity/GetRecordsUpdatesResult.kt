package com.qwert2603.spenddemo.model.rest.entity

data class GetRecordsUpdatesResult(
        val updatedRecords: List<RecordServer>,
        val deletedRecordsUuid: List<String>,
        val lastUpdateInfo: LastUpdateInfoServer
)