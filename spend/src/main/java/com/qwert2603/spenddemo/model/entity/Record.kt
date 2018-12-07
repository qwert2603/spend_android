package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.model.hashCodeLong
import com.qwert2603.spenddemo.utils.Const

data class Record(
        val uuid: String,
        val recordCategory: RecordCategory,
        val date: SDate,
        val time: STime?,
        val kind: String,
        val value: Int,
        val change: RecordChange?
) : RecordsListItem {

    override val id = uuid.hashCodeLong()

    init {
        require(value > 0)
        require(kind.length in 1..Const.MAX_RECORD_KIND_LENGTH)
    }

    fun equalIgnoreChange(other: Record) = uuid == other.uuid
            && recordCategory == other.recordCategory
            && date == other.date
            && time == other.time
            && kind == other.kind
            && value == other.value

    fun isDeleted() = change?.isDelete == true
}