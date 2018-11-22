package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime

sealed class DraftPartialChange : PartialChange {
    data class DateSelected(val date: SDate?) : DraftPartialChange()
    data class TimeSelected(val time: STime?) : DraftPartialChange()
    data class KindChanged(val kind: String) : DraftPartialChange()
    data class KindSelected(val kind: String, val lastValue: Int) : DraftPartialChange()
    data class ValueChanged(val value: Int) : DraftPartialChange()
    object DraftCleared : DraftPartialChange()
}