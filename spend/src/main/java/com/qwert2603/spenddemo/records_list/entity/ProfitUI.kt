package com.qwert2603.spenddemo.records_list.entity

import com.qwert2603.spenddemo.model.entity.Profit
import java.util.*

data class ProfitUI(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date
) : RecordsListItem

fun Profit.toProfitUI() = ProfitUI(id, kind, value, date)