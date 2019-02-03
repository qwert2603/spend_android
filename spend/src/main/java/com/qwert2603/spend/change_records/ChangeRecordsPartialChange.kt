package com.qwert2603.spend.change_records

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime
import com.qwert2603.spend.utils.Wrapper

sealed class ChangeRecordsPartialChange : PartialChange {
    data class ChangedDateSelected(val date: SDate?) : ChangeRecordsPartialChange()
    data class ChangedTimeSelected(val time: Wrapper<STime>?) : ChangeRecordsPartialChange()
}