package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.utils.FastDiffUtils

sealed class RecordsListPartialChange : PartialChange {
    data class RecordsListChanged(
            val list: List<RecordsListItem>,
            val diff: FastDiffUtils.FastDiffResult,
            val recordsChanges: HashMap<String, RecordChange>
    ) : RecordsListPartialChange()

    data class ShowInfoChanged(val showInfo: ShowInfo) : RecordsListPartialChange()
    data class SumsInfoChanged(val sumsInfo: SumsInfo) : RecordsListPartialChange()
    data class LongSumPeriodChanged(val days: Days) : RecordsListPartialChange()
    data class ShortSumPeriodChanged(val minutes: Minutes) : RecordsListPartialChange()

    data class SyncStateChanged(val syncState: SyncState) : RecordsListPartialChange()
}