package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.utils.Const

data class DraftViewState(
        val creatingRecord: CreatingRecord,
        val createEnable: Boolean
) {
    val valueString: String = creatingRecord.value.takeIf { it != 0 }?.toString() ?: ""
    val dateString: String = Const.DATE_FORMAT.format(creatingRecord.date)
}