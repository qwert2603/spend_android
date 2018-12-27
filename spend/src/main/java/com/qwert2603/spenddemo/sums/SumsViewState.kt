package com.qwert2603.spenddemo.sums

import com.qwert2603.spenddemo.model.entity.RecordsListItem
import com.qwert2603.spenddemo.model.entity.SumsShowInfo
import com.qwert2603.spenddemo.model.entity.SyncState
import com.qwert2603.spenddemo.utils.FastDiffUtils

data class SumsViewState(
        val sumsShowInfo: SumsShowInfo,
        val records: List<RecordsListItem>,
        val diff: FastDiffUtils.FastDiffResult,
        val syncState: SyncState
)