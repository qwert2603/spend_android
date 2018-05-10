package com.qwert2603.spenddemo.model.entity

data class SpendsState(
        val spends: List<Spend>,
        val syncStatuses: Map<Long, SyncStatus>,
        val changeKinds: Map<Long, ChangeKind>
)