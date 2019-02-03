package com.qwert2603.spend.model.entity

import com.qwert2603.spend.utils.Const

data class RecordCategory(
        val uuid: String,
        val recordTypeId: Long,
        val name: String
) {
    init {
        require(recordTypeId in Const.RECORD_TYPE_IDS)
        require(name.length in 1..Const.MAX_CATEGORY_NAME_LENGTH)
    }
}