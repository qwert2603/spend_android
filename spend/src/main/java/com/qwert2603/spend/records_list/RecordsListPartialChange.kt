package com.qwert2603.spend.records_list

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.utils.FastDiffUtils

sealed class RecordsListPartialChange : PartialChange {
    data class RecordsListChanged(
            val list: List<RecordsListItem>,
            val diff: FastDiffUtils.FastDiffResult,
            val recordsChanges: HashMap<String, RecordChange>
    ) : RecordsListPartialChange()

    data class ShowInfoChanged(val showInfo: ShowInfo) : RecordsListPartialChange()
    data class SumsInfoChanged(val sumsInfo: SumsInfo) : RecordsListPartialChange()
    data class SortByValueChanged(val sortByValue: Boolean) : RecordsListPartialChange()
    data class LongSumPeriodChanged(val days: Days) : RecordsListPartialChange()
    data class ShortSumPeriodChanged(val minutes: Minutes) : RecordsListPartialChange()
    data class ShowFiltersChanged(val show: Boolean) : RecordsListPartialChange()

    data class SyncStateChanged(val syncState: SyncState) : RecordsListPartialChange()

    data class ToggleRecordSelection(val recordUuid: String) : RecordsListPartialChange()
    object ClearSelection : RecordsListPartialChange()

    data class SearchQueryChanged(val search: String) : RecordsListPartialChange()
    data class StartDateChanged(val startDate: SDate?) : RecordsListPartialChange()
    data class EndDateChanged(val endDate: SDate?) : RecordsListPartialChange()
}