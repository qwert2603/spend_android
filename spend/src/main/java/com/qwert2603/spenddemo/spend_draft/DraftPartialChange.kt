package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.PartialChange

sealed class DraftPartialChange : PartialChange {
    data class DateSelected(val date: Int?) : DraftPartialChange()
    data class TimeSelected(val time: Int?) : DraftPartialChange()
    data class KindChanged(val kind: String) : DraftPartialChange()
    data class KindSelected(val kind: String, val lastValue: Int) : DraftPartialChange()
    data class ValueChanged(val value: Int) : DraftPartialChange()
    object DraftCleared : DraftPartialChange()
}