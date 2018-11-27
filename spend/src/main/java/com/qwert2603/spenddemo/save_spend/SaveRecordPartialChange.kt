package com.qwert2603.spenddemo.save_spend

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime

sealed class SaveRecordPartialChange : PartialChange {
    data class EditingRecordLoaded(val recordDraft: RecordDraft) : SaveRecordPartialChange()

    data class KindChanged(val kind: String) : SaveRecordPartialChange()
    data class ValueChanged(val value: Int) : SaveRecordPartialChange()

    data class KindSelected(val kind: String, val lastValue: Int) : SaveRecordPartialChange()
    data class DateSelected(val date: SDate?) : SaveRecordPartialChange()
    data class TimeSelected(val time: STime?) : SaveRecordPartialChange()

    // after new record created.
    object DraftCleared : SaveRecordPartialChange()

    data class KindChangeOnServer(val kind: String) : SaveRecordPartialChange()
    data class ValueChangeOnServer(val value: Int) : SaveRecordPartialChange()
    data class DateChangeOnServer(val date: SDate) : SaveRecordPartialChange()
    data class TimeChangeOnServer(val time: STime?) : SaveRecordPartialChange()

    data class RecordJustChangedOnServer(val justChanged: Boolean) : SaveRecordPartialChange()

    data class ExistingRecordChanged(val existingRecord: RecordDraft) : SaveRecordPartialChange()

    data class KindServerResolved(val acceptFromServer: Boolean) : SaveRecordPartialChange()
    data class ValueServerResolved(val acceptFromServer: Boolean) : SaveRecordPartialChange()
    data class DateServerResolved(val acceptFromServer: Boolean) : SaveRecordPartialChange()
    data class TimeServerResolved(val acceptFromServer: Boolean) : SaveRecordPartialChange()
}