package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.base_mvi.PartialChange
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import java.util.*

sealed class DraftPartialChange : PartialChange {
    data class DraftLoaded(val creatingRecord: CreatingRecord) : DraftPartialChange()
    data class KindChanged(val kind: String) : DraftPartialChange()
    data class ValueChanged(val value: Int) : DraftPartialChange()
    data class DateChanged(val date: Date) : DraftPartialChange()
    data class DraftCleared(private val ignored: Unit = Unit) : DraftPartialChange()
    data class CreateEnableChanged(val canCreated: Boolean) : DraftPartialChange()
}