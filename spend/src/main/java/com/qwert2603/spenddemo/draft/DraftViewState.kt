package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.model.entity.CreatingRecord

data class DraftViewState(
        val creatingRecord: CreatingRecord,
        val createEnable: Boolean
) {
    val valueString: String = creatingRecord.value.takeIf { it != 0 }?.toString() ?: ""
}