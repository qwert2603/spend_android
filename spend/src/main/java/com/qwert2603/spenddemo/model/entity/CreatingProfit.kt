package com.qwert2603.spenddemo.model.entity

import com.qwert2603.spenddemo.utils.onlyDate
import com.qwert2603.spenddemo.utils.onlyTime
import java.util.*

data class CreatingProfit(
        val kind: String,
        val value: Int,
        val date: Date?, // null means "now".
        val time: Date?
)

fun CreatingProfit.toProfit(id: Long) =
        if (date != null) {
            Profit(id, kind, value, date, time)
        } else {
            val now = Date()
            Profit(id, kind, value, now.onlyDate(), now.onlyTime())
        }