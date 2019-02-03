package com.qwert2603.spend.model.local_db.results

import com.qwert2603.spend.model.entity.*

data class RecordItemResult(
        val uuid: String,
        val recordCategoryUuid: String,
        val recordTypeId: Long,
        val recordCategoryName: String,
        val date: Int,
        val time: Int?,
        val kind: String,
        val value: Int,
        val changeId: Long?,
        val isDelete: Boolean?
) {
    fun toRecord() = Record(
            uuid = uuid,
            recordCategory = RecordCategory(
                    uuid = recordCategoryUuid,
                    recordTypeId = recordTypeId,
                    name = recordCategoryName
            ),
            date = date.toSDate(),
            time = time?.toSTime(),
            kind = kind,
            value = value,
            change = changeId?.let {
                RecordChange(
                        id = it,
                        isDelete = isDelete!!
                )
            }
    )
}