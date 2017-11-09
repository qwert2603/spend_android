package com.qwert2603.spenddemo.model.entity

data class RecordsState(
        val records: List<Record>,
        val syncStatuses: Map<Long, SyncStatus>,
        val changeKinds: Map<Long, ChangeKind>
)