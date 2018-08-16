package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.model.IdentifiableLong
import java.util.*

// todo: для статистики добавить поле "категория" в таблицы расходов и доходов.
data class Spend(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        val time: Date?
) : IdentifiableLong