package com.qwert2603.spend.model.entity

import com.qwert2603.spend.utils.Const
import com.qwert2603.spend.utils.DateUtils
import java.util.*

data class RecordDraft(
        val isNewRecord: Boolean,
        val uuid: String,
        val recordTypeId: Long,
        val recordCategoryUuid: String?,
        val recordCategoryName: String, // input in text field.
        val date: SDate?, // null means "now".
        val time: STime?,
        val kind: String,
        val value: Int
) {
    init {
        require(recordTypeId in Const.RECORD_TYPE_IDS)

        if (date == null) require(time == null)
    }

    fun isValid() = recordCategoryUuid != null
            && value > 0
            && kind.length in 1..Const.MAX_RECORD_KIND_LENGTH
            && (dateTime().first + Const.CHANGE_RECORD_PAST) > DateUtils.getNow().first

    fun dateTime(): Pair<SDate, STime?> =
            if (date == null) DateUtils.getNow()
            else date to time

    companion object {
        fun new(recordTypeId: Long) = RecordDraft(
                isNewRecord = true,
                uuid = UUID.randomUUID().toString(),
                recordTypeId = recordTypeId,
                recordCategoryUuid = null,
                recordCategoryName = "",
                date = null,
                time = null,
                kind = "",
                value = 0
        )
    }
}

fun Record.toRecordDraft() = RecordDraft(
        isNewRecord = false,
        uuid = uuid,
        recordTypeId = recordCategory.recordTypeId,
        recordCategoryUuid = recordCategory.uuid,
        recordCategoryName = recordCategory.name,
        date = date,
        time = time,
        kind = kind,
        value = value
)