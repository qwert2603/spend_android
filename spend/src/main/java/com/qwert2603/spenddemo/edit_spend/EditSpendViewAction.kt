package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spenddemo.model.entity.Spend

sealed class EditSpendViewAction : ViewAction {
    object FocusOnKindInput : EditSpendViewAction()
    object FocusOnValueInput : EditSpendViewAction()
    data class AskToSelectDate(val millis: Long) : EditSpendViewAction()
    data class AskToSelectTime(val millis: Long) : EditSpendViewAction()
    object AskToSelectKind : EditSpendViewAction()
    object EditingSpendDeletedOnServer : EditSpendViewAction()
    data class SendResult(val spend: Spend) : EditSpendViewAction()
}