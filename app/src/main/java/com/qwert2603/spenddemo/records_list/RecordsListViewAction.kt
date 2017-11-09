package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.base_mvi.ViewAction
import com.qwert2603.spenddemo.records_list.entity.RecordUI

sealed class RecordsListViewAction : ViewAction {
    data class MoveToChangesScreen(private val ignored: Unit = Unit) : RecordsListViewAction()
    data class AskToDeleteRecord(val id: Long, val text: String) : RecordsListViewAction()
    data class AskToEditRecord(val record: RecordUI) : RecordsListViewAction()
    data class ScrollToPosition(val position: Int) : RecordsListViewAction()
}