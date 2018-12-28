package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spenddemo.model.entity.Days
import com.qwert2603.spenddemo.model.entity.Minutes

sealed class RecordsListViewAction : ViewAction {
    data class AskForRecordActions(val recordUuid: String) : RecordsListViewAction()
    data class AskToCreateRecord(val recordTypeId: Long) : RecordsListViewAction()
    data class AskToEditRecord(val recordUuid: String) : RecordsListViewAction()
    data class AskToDeleteRecord(val recordUuid: String) : RecordsListViewAction()
    data class AskToChooseLongSumPeriod(val days: Days) : RecordsListViewAction()
    data class AskToChooseShortSumPeriod(val minutes: Minutes) : RecordsListViewAction()
    data class OnRecordCreatedLocally(val uuid: String) : RecordsListViewAction()
    data class OnRecordEditedLocally(val uuid: String) : RecordsListViewAction()
    object RerenderAll : RecordsListViewAction()
    data class AskToCombineRecords(
            val recordUuids: List<String>,
            val categoryUuid: String,
            val kind: String
    ) : RecordsListViewAction()
}