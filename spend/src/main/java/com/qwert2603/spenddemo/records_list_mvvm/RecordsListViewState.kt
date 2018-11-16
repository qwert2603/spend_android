package com.qwert2603.spenddemo.records_list_mvvm

import com.qwert2603.spenddemo.model.entity.RecordsListItem
import com.qwert2603.spenddemo.utils.FastDiffUtils

data class RecordsListViewState(
        val records: List<RecordsListItem>,
        val diff: FastDiffUtils.FastDiffResult,
        val showInfo: ShowInfo,
        val longSumPeriodDays: Int,
        val shortSumPeriodMinutes: Int,
        val sumsInfo: SumsInfo,
        val syncingRecordsUuids: Set<String>
)