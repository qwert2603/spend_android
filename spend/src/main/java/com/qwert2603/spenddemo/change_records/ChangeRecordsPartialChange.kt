package com.qwert2603.spenddemo.change_records

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.utils.Wrapper

sealed class ChangeRecordsPartialChange : PartialChange {
    data class ChangedDateSelected(val date: SDate?) : ChangeRecordsPartialChange()
    data class ChangedTimeSelected(val time: Wrapper<STime>?) : ChangeRecordsPartialChange()
}