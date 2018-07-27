package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spenddemo.model.entity.CreatingSpend

sealed class DraftPartialChange : PartialChange {
    data class DraftChanged(val creatingSpend: CreatingSpend, val createEnable: Boolean) : DraftPartialChange()
    object CurrentDateChanged : DraftPartialChange()
}