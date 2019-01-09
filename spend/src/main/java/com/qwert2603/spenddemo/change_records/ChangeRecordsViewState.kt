package com.qwert2603.spenddemo.change_records

import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.utils.Wrapper

data class ChangeRecordsViewState(
        val changedDate: SDate?,
        val changedTime: Wrapper<STime>?
) {
    fun isChangeEnable() = listOfNotNull(changedDate, changedTime).isNotEmpty()
}