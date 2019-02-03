package com.qwert2603.spend.change_records

import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime

sealed class ChangeRecordsViewAction : ViewAction {
    data class AskToSelectDate(val date: SDate, val minDate: SDate) : ChangeRecordsViewAction()
    data class AskToSelectTime(val time: STime) : ChangeRecordsViewAction()
    object RerenderAll : ChangeRecordsViewAction()
    object Close : ChangeRecordsViewAction()
}