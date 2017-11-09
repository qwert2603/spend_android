package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.base_mvi.ViewAction

sealed class DraftViewAction : ViewAction {
    data class FocusOnKindInput(private val ignored: Unit = Unit) : DraftViewAction()
    data class AskToSelectDate(val millis: Long) : DraftViewAction()
    data class AskToSelectKind(private val ignored: Unit = Unit) : DraftViewAction()
}