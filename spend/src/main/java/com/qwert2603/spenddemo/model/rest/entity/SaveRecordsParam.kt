package com.qwert2603.spenddemo.model.rest.entity

data class SaveRecordsParam(
        val updatedRecords: List<RecordServer>,
        val deletedRecordsUuid: List<String>
)