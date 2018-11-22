package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime

sealed class DraftViewAction : ViewAction {
    object FocusOnKindInput : DraftViewAction()
    object FocusOnValueInput : DraftViewAction()
    data class AskToSelectDate(val date: SDate) : DraftViewAction()
    data class AskToSelectTime(val time: STime) : DraftViewAction()
    object AskToSelectKind : DraftViewAction()
    data class ShowKindSuggestions(val suggestions: List<String>, val search: String) : DraftViewAction()
    object HideKindSuggestions : DraftViewAction()
    object RerenderAll : DraftViewAction()
}