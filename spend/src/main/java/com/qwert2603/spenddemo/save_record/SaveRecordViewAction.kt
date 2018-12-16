package com.qwert2603.spenddemo.save_record

import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spenddemo.model.entity.RecordCategoryAggregation
import com.qwert2603.spenddemo.model.entity.RecordKindAggregation
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime

sealed class SaveRecordViewAction : ViewAction {
    object FocusOnCategoryInput : SaveRecordViewAction()
    object FocusOnKindInput : SaveRecordViewAction()
    object FocusOnValueInput : SaveRecordViewAction()
    data class AskToSelectDate(val date: SDate, val minDate: SDate) : SaveRecordViewAction()
    data class AskToSelectTime(val time: STime) : SaveRecordViewAction()
    data class AskToSelectCategory(val recordTypeId: Long) : SaveRecordViewAction()
    data class AskToSelectKind(val recordTypeId: Long, val categoryUuid: String?) : SaveRecordViewAction()
    data class ShowCategorySuggestions(val suggestions: List<RecordCategoryAggregation>, val search: String) : SaveRecordViewAction()
    object HideCategorySuggestions : SaveRecordViewAction()
    data class ShowKindSuggestions(val suggestions: List<RecordKindAggregation>, val search: String, val withCategory: Boolean) : SaveRecordViewAction()
    object HideKindSuggestions : SaveRecordViewAction()
    data class EditingRecordDeletedOnServer(val recordTypeId: Long) : SaveRecordViewAction()
    object EditingRecordNotFound : SaveRecordViewAction()
    object RerenderAll : SaveRecordViewAction()
    object Close : SaveRecordViewAction()
}