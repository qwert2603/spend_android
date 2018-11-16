package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.andrlib.base.mvi.ViewAction

sealed class SaveRecordViewAction : ViewAction {
    object FocusOnKindInput : SaveRecordViewAction()
    object FocusOnValueInput : SaveRecordViewAction()
    data class AskToSelectDate(val date: Int) : SaveRecordViewAction()
    data class AskToSelectTime(val time: Int) : SaveRecordViewAction()
    data class AskToSelectKind(val recordTypeId: Long) : SaveRecordViewAction()
    data class EditingRecordDeletedOnServer(val recordTypeId: Long) : SaveRecordViewAction()
    object RerenderAll : SaveRecordViewAction()
    object Close : SaveRecordViewAction()
}