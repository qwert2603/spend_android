package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.model.hashCodeLong
import com.qwert2603.spenddemo.utils.Const

data class Record(
        val uuid: String,
        val recordTypeId: Long,
        val date: Int, // format is "yyyyMMdd"
        val time: Int?, // format is "HHmm"
        val kind: String,
        val value: Int,
        val change: RecordChange?
) : RecordsListItem {

    override val id = uuid.hashCodeLong()

    init {
        require(recordTypeId in Const.RECORD_TYPE_IDS)
        require(value > 0)
        require(kind.length <= Const.MAX_KIND_LENGTH)
    }

    fun equalIgnoreChange(other: Record) = uuid == other.uuid
            && recordTypeId == other.recordTypeId
            && date == other.date
            && time == other.time
            && kind == other.kind
            && value == other.value
}