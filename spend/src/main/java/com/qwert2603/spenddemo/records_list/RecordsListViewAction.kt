package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spenddemo.records_list.entity.RecordUI

sealed class RecordsListViewAction : ViewAction {
    object MoveToChangesScreen : RecordsListViewAction()
    data class AskToDeleteRecord(val id: Long) : RecordsListViewAction()
    data class AskToEditRecord(val record: RecordUI) : RecordsListViewAction()
    data class ScrollToRecordAndHighlight(val recordId: Long) : RecordsListViewAction()
    data class ScrollToProfitAndHighlight(val profitId: Long) : RecordsListViewAction()
    data class SendRecords(val text: String) : RecordsListViewAction()
    object ShowAbout : RecordsListViewAction()
    object OpenAddProfitDialog : RecordsListViewAction()
    data class AskToDeleteProfit(val id: Long) : RecordsListViewAction()
}