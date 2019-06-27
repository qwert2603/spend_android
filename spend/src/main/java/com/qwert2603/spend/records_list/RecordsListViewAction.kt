package com.qwert2603.spend.records_list

import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spend.model.entity.Days
import com.qwert2603.spend.model.entity.Minutes
import com.qwert2603.spend.model.entity.SDate

sealed class RecordsListViewAction : ViewAction {
    data class AskForRecordActions(val recordUuid: String) : RecordsListViewAction()
    data class AskToCreateRecord(val recordTypeId: Long) : RecordsListViewAction()
    data class AskToChooseLongSumPeriod(val days: Days) : RecordsListViewAction()
    data class AskToChooseShortSumPeriod(val minutes: Minutes) : RecordsListViewAction()
    data class OnRecordCreatedLocally(val uuid: String) : RecordsListViewAction()
    data class OnRecordEditedLocally(val uuid: String) : RecordsListViewAction()
    data class OnRecordCombinedLocally(val uuid: String) : RecordsListViewAction()
    object RerenderAll : RecordsListViewAction()

    data class AskToCombineRecords(
            val recordUuids: List<String>,
            val categoryUuid: String,
            val kind: String
    ) : RecordsListViewAction()

    data class AskToDeleteRecords(val recordUuids: List<String>) : RecordsListViewAction()
    data class AskToChangeRecords(val recordUuids: List<String>) : RecordsListViewAction()

    object ScrollToTop : RecordsListViewAction()

    data class AskToSelectStartDate(val startDate: SDate, val maxDate: SDate?) : RecordsListViewAction()
    data class AskToSelectEndDate(val endDate: SDate, val minDate: SDate?) : RecordsListViewAction()
}