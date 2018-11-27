package com.qwert2603.spenddemo.save_spend

import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.Wrapper
import com.qwert2603.spenddemo.utils.toPointedString

data class SaveRecordViewState(
        val isNewRecord: Boolean,
        val recordDraft: RecordDraft,
        val serverKind: String?,
        val serverDate: SDate?,
        val serverTime: Wrapper<STime?>?,
        val serverValue: Int?,
        val justChangedOnServer: Boolean,
        val existingRecord: RecordDraft? // null, when creating new record
) {
    companion object {
        val DRAFT_IS_LOADING = RecordDraft(
                isNewRecord = true,
                uuid = "DRAFT_IS_LOADING",
                recordTypeId = Const.RECORD_TYPE_ID_SPEND,
                date = null,
                time = null,
                kind = "",
                value = 0
        )
    }

    init {
        if (isNewRecord) require(existingRecord == null)
    }

    val valueString: String = recordDraft.value.takeIf { it != 0 }?.toPointedString() ?: ""

    private val canSave = recordDraft.isValid()
    val isSaveEnable = canSave && !justChangedOnServer && recordDraft != existingRecord
}