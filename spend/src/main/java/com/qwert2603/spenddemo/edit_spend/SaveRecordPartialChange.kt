package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.andrlib.base.mvi.PartialChange

sealed class SaveRecordPartialChange : PartialChange {
    data class KindChanged(val kind: String) : SaveRecordPartialChange()
    data class ValueChanged(val value: Int) : SaveRecordPartialChange()

    data class KindSelected(val kind: String) : SaveRecordPartialChange()
    data class DateSelected(val date: Int?) : SaveRecordPartialChange()
    data class TimeSelected(val time: Int?) : SaveRecordPartialChange()

    data class KindChangeOnServer(val kind: String) : SaveRecordPartialChange()
    data class ValueChangeOnServer(val value: Int) : SaveRecordPartialChange()
    data class DateChangeOnServer(val date: Int) : SaveRecordPartialChange()
    data class TimeChangeOnServer(val time: Int?) : SaveRecordPartialChange()

    data class RecordJustChangedOnServer(val justChanged: Boolean) : SaveRecordPartialChange()

    data class KindServerResolved(val acceptFromServer: Boolean) : SaveRecordPartialChange()
    data class ValueServerResolved(val acceptFromServer: Boolean) : SaveRecordPartialChange()
    data class DateServerResolved(val acceptFromServer: Boolean) : SaveRecordPartialChange()
    data class TimeServerResolved(val acceptFromServer: Boolean) : SaveRecordPartialChange()
}