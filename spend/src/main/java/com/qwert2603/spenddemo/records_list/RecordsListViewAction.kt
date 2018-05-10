package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spenddemo.records_list.entity.ProfitUI
import com.qwert2603.spenddemo.records_list.entity.SpendUI

sealed class RecordsListViewAction : ViewAction {
    object MoveToChangesScreen : RecordsListViewAction()
    data class AskToDeleteSpend(val id: Long) : RecordsListViewAction()
    data class AskToEditSpend(val spend: SpendUI) : RecordsListViewAction()
    data class ScrollToSpendAndHighlight(val spendId: Long) : RecordsListViewAction()
    data class ScrollToProfitAndHighlight(val profitId: Long) : RecordsListViewAction()
    data class SendRecords(val text: String) : RecordsListViewAction()
    object ShowAbout : RecordsListViewAction()
    object OpenAddProfitDialog : RecordsListViewAction()
    data class AskToEditProfit(val profitUI: ProfitUI) : RecordsListViewAction()
    data class AskToDeleteProfit(val id: Long) : RecordsListViewAction()
}