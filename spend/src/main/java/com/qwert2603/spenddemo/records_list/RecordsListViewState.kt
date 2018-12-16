package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.entity.RecordsListItem
import com.qwert2603.spenddemo.model.entity.SyncState
import com.qwert2603.spenddemo.utils.FastDiffUtils

data class RecordsListViewState(
        val showInfo: ShowInfo,
        val longSumPeriodDays: Int,
        val shortSumPeriodMinutes: Int,
        val sumsInfo: SumsInfo,
        val records: List<RecordsListItem>,
        val diff: FastDiffUtils.FastDiffResult,
        val recordsChanges: HashMap<String, RecordChange>, // key is Record::uuid
        val syncState: SyncState
)