package com.qwert2603.spenddemo.model.entity

import com.qwert2603.spenddemo.utils.secondsToZero
import java.util.*

data class CreatingSpend(
        val kind: String,
        val value: Int,
        val date: Date?
) {
    fun getDateNN() = date ?: Date().secondsToZero()
}

fun CreatingSpend.toSpend(id: Long) = Spend(id, kind, value, getDateNN())