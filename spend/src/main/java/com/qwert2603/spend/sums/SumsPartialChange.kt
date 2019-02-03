package com.qwert2603.spend.sums

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spend.model.entity.RecordsListItem
import com.qwert2603.spend.model.entity.SumsShowInfo
import com.qwert2603.spend.model.entity.SyncState
import com.qwert2603.spend.utils.FastDiffUtils

sealed class SumsPartialChange : PartialChange {
    data class RecordsListChanged(
            val list: List<RecordsListItem>,
            val diff: FastDiffUtils.FastDiffResult
    ) : SumsPartialChange()

    data class ShowInfoChanged(val sumsShowInfo: SumsShowInfo) : SumsPartialChange()
    data class SyncStateChanged(val syncState: SyncState) : SumsPartialChange()
}