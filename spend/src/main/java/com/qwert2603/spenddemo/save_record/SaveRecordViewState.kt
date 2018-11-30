package com.qwert2603.spenddemo.save_record

import com.qwert2603.spenddemo.model.entity.RecordCategory
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.Wrapper

data class SaveRecordViewState(
        val isNewRecord: Boolean,
        val recordDraft: RecordDraft,
        val serverCategory: RecordCategory?,
        val serverKind: String?,
        val serverDate: SDate?,
        val serverTime: Wrapper<STime?>?,
        val serverValue: Int?,
        val justChangedOnServer: Boolean,
        val existingRecord: RecordDraft? // null, when creating new record
) {
    companion object {
        val DRAFT_IS_LOADING = RecordDraft(
                isNewRecord = false,
                uuid = "DRAFT_IS_LOADING",
                recordTypeId = Const.RECORD_TYPE_ID_SPEND,
                recordCategoryUuid = null,
                recordCategoryName = "",
                date = null,
                time = null,
                kind = "",
                value = 0
        )
    }

    init {
        if (isNewRecord) {
            require(!justChangedOnServer)
            require(existingRecord == null)
        }
    }

    val valueString: String = recordDraft.value.takeIf { it != 0 }?.toString() ?: "" // todo: toPointedString

    fun isSaveEnable() = recordDraft.isValid() && !justChangedOnServer && recordDraft != existingRecord

    fun categorySelected() = recordDraft.recordCategoryUuid != null
}