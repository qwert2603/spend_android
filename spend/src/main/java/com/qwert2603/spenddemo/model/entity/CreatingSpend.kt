package com.qwert2603.spenddemo.model.entity

import java.util.*

data class CreatingSpend(
        val kind: String,
        val value: Int,
        val date: Date?
) {
    fun getDateNN() = date ?: Date()
}

fun CreatingSpend.toSpend(id: Long) = Spend(id, kind, value, getDateNN())
fun Spend.toCreatingSpend() = CreatingSpend(kind, value, date)