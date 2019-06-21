package com.qwert2603.spend.change_records

import com.qwert2603.spend.model.entity.OldRecordsLockState
import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime
import com.qwert2603.spend.model.entity.plus
import com.qwert2603.spend.utils.Const
import com.qwert2603.spend.utils.DateUtils
import com.qwert2603.spend.utils.Wrapper

data class ChangeRecordsViewState(
        val changedDate: SDate?,
        val changedTime: Wrapper<STime>?,
        val oldRecordsLockState: OldRecordsLockState
) {
    fun isChangeEnable() = listOfNotNull(changedDate, changedTime).isNotEmpty()
            && (changedDate == null || !oldRecordsLockState.isLocked || (changedDate + Const.CHANGE_RECORD_PAST) > DateUtils.getNow().first)
}