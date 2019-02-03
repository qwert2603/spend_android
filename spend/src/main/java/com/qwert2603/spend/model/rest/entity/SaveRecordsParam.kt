package com.qwert2603.spend.model.rest.entity

data class SaveRecordsParam(
        val updatedRecords: List<RecordServer>,
        val deletedRecordsUuid: List<String>
)