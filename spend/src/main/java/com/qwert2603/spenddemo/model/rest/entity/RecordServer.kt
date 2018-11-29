package com.qwert2603.spenddemo.model.rest.entity

data class RecordServer(
        val uuid: String,
        val recordCategoryUuid: String,
        val date: Int,
        val time: Int?,
        val kind: String,
        val value: Int
)