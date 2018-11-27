package com.qwert2603.spenddemo.save_spend

import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime

sealed class SaveRecordViewAction : ViewAction {
    object FocusOnKindInput : SaveRecordViewAction()
    object FocusOnValueInput : SaveRecordViewAction()
    data class AskToSelectDate(val date: SDate) : SaveRecordViewAction()
    data class AskToSelectTime(val time: STime) : SaveRecordViewAction()
    data class AskToSelectKind(val recordTypeId: Long) : SaveRecordViewAction()
    data class EditingRecordDeletedOnServer(val recordTypeId: Long) : SaveRecordViewAction()
    object EditingRecordNotFound : SaveRecordViewAction()
    object RerenderAll : SaveRecordViewAction()
    object Close : SaveRecordViewAction()
}