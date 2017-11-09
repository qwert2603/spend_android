package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.utils.Const
import java.util.*

data class DraftViewState(
        val kind: String,
        val value: Int,
        val date: Date,
        val createEnable: Boolean
) {
    val valueString: String = value.takeIf { it != 0 }?.toString() ?: ""
    val dateString: String = Const.DATE_FORMAT.format(date)
}