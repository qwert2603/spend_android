package com.qwert2603.spend.about

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spend.model.entity.OldRecordsLockState

sealed class AboutPartialChange : PartialChange {
    object MakingDumpStarted : AboutPartialChange()
    object MakingDumpFinished : AboutPartialChange()
    data class OldRecordsLockStateChanged(val oldRecordsLockState: OldRecordsLockState) : AboutPartialChange()
}