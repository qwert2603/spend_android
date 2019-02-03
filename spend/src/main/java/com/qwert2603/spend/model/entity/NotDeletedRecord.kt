package com.qwert2603.spend.model.entity

data class NotDeletedRecord(
        val uuid: String,
        val recordCategoryUuid: String,
        val date: Int,
        val time: Int?,
        val kind: String,
        val value: Int
)

fun Record.toNotDeletedRecord() = NotDeletedRecord(
        uuid = uuid,
        recordCategoryUuid = recordCategory.uuid,
        date = date.date,
        time = time?.time,
        kind = kind,
        value = value
)