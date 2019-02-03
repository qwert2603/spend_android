package com.qwert2603.spend.change_records

import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime
import com.qwert2603.spend.utils.Wrapper

data class ChangeRecordsViewState(
        val changedDate: SDate?,
        val changedTime: Wrapper<STime>?
) {
    fun isChangeEnable() = listOfNotNull(changedDate, changedTime).isNotEmpty()
}