package com.qwert2603.spend.about

import com.qwert2603.spend.model.entity.OldRecordsLockState

data class AboutViewState(
        val isMakingDump: Boolean,
        val oldRecordsLockState: OldRecordsLockState
)