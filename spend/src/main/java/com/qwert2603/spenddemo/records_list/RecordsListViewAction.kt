package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.ViewAction

sealed class RecordsListViewAction : ViewAction {
    data class AskToCreateRecord(val recordTypeId: Long) : RecordsListViewAction()
    data class AskToEditRecord(val recordUuid: String) : RecordsListViewAction()
    data class AskToDeleteRecord(val recordUuid: String) : RecordsListViewAction()
    data class AskToChooseLongSumPeriod(val days: Int) : RecordsListViewAction()
    data class AskToChooseShortSumPeriod(val minutes: Int) : RecordsListViewAction()
    data class OnRecordCreatedLocally(val uuid: String) : RecordsListViewAction()
    object ShowDumpIsCreating : RecordsListViewAction()
    data class SendDump(val dump: String) : RecordsListViewAction()
    object RerenderAll : RecordsListViewAction()
}