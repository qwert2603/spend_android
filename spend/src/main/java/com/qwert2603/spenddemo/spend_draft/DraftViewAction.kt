package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.ViewAction

sealed class DraftViewAction : ViewAction {
    object FocusOnKindInput : DraftViewAction()
    object FocusOnValueInput : DraftViewAction()
    data class AskToSelectDate(val millis: Long) : DraftViewAction()
    data class AskToSelectTime(val millis: Long) : DraftViewAction()
    object AskToSelectKind : DraftViewAction()
    data class ShowKindSuggestions(val suggestions: List<String>, val search: String) : DraftViewAction()
    object HideKindSuggestions : DraftViewAction()
}