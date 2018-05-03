package com.qwert2603.spenddemo.model.entity

import java.util.*

data class CreatingProfit(
        val kind: String,
        val value: Int,
        val date: Date
)

fun CreatingProfit.toProfit(id: Long) = Profit(id, kind, value, date)
fun Profit.toCreatingProfit() = CreatingProfit(kind, value, date)