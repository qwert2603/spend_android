package com.qwert2603.spend.sums

import com.qwert2603.spend.model.entity.RecordsListItem
import com.qwert2603.spend.model.entity.SumsShowInfo
import com.qwert2603.spend.model.entity.SyncState
import com.qwert2603.spend.utils.FastDiffUtils

data class SumsViewState(
        val sumsShowInfo: SumsShowInfo,
        val records: List<RecordsListItem>,
        val diff: FastDiffUtils.FastDiffResult,
        val syncState: SyncState
)