package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.utils.toPointedString

data class DraftViewState(
        val recordDraft: RecordDraft
) {
    val valueString: String = recordDraft.value.takeIf { it != 0 }?.toPointedString() ?: ""
}