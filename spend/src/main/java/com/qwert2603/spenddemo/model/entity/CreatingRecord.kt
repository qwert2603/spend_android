package com.qwert2603.spenddemo.model.entity

import java.util.Date

data class CreatingRecord(
        val kind: String,
        val value: Int,
        val date: Date,
        val dateSet: Boolean
)

fun CreatingRecord.toRecord(id: Long) = Record(id, kind, value, date)
fun Record.toCreatingRecord() = CreatingRecord(kind, value, date, true)