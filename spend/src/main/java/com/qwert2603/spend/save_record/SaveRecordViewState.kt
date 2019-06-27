package com.qwert2603.spend.save_record

import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.utils.Const
import com.qwert2603.spend.utils.Wrapper
import com.qwert2603.spend.utils.toPointedString

data class SaveRecordViewState(
        val isNewRecord: Boolean,
        val recordDraft: RecordDraft,
        val serverCategory: RecordCategory?,
        val serverKind: String?,
        val serverDate: SDate?,
        val serverTime: Wrapper<STime>?,
        val serverValue: Int?,
        private val justChangedOnServer: Boolean,
        private val existingRecord: RecordDraft?, // null, when creating new record
        val oldRecordsLockState: OldRecordsLockState
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
            require(serverCategory == null)
            require(serverKind == null)
            require(serverDate == null)
            require(serverTime == null)
            require(serverValue == null)
            require(!justChangedOnServer)
            require(existingRecord == null)
        }
    }

    val valueString: String = recordDraft.value.takeIf { it != 0 }?.toPointedString() ?: ""

    fun isSaveEnable() = recordDraft.isValid(oldRecordsLockState.isLocked)
            && listOfNotNull(serverCategory, serverKind, serverDate, serverTime, serverValue).isEmpty()
            && !justChangedOnServer
            && recordDraft != existingRecord

    fun categorySelected() = recordDraft.recordCategoryUuid != null
}