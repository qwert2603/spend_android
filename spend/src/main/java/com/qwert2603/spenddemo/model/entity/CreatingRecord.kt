package com.qwert2603.spenddemo.model.entity

import java.util.*

data class CreatingRecord(
        val kind: String,
        val value: Int,
        val date: Date?
) {
    fun getDateNN() = date ?: Date()
}

fun CreatingRecord.toRecord(id: Long) = Record(id, kind, value, getDateNN())
fun Record.toCreatingRecord() = CreatingRecord(kind, value, date)