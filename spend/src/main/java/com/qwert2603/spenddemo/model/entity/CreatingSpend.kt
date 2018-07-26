package com.qwert2603.spenddemo.model.entity

import com.qwert2603.spenddemo.utils.onlyDate
import com.qwert2603.spenddemo.utils.onlyTime
import java.util.*

data class CreatingSpend(
        val kind: String,
        val value: Int,
        val date: Date?, // null means "now".
        val time: Date?
) {
    init {
        if (date == null) require(time == null)
    }

    companion object {
        val EMPTY = CreatingSpend("", 0, null, null)
    }
}

fun CreatingSpend.toSpend(id: Long) =
        if (date != null) {
            Spend(id, kind, value, date, time)
        } else {
            val now = Date()
            Spend(id, kind, value, now.onlyDate(), now.onlyTime())
        }