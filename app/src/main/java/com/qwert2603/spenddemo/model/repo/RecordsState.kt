package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.SyncStatus

data class RecordsState(
        val records: List<Record>,
        val syncStatuses: Map<Long, SyncStatus>,
        val changeKinds: Map<Long, ChangeKind>
)