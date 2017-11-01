package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.SyncStatus
import com.qwert2603.spenddemo.model.repo.RecordsState

sealed class RecordsStatePartialChange {
    data class InitLoaded(val recordsState: RecordsState) : RecordsStatePartialChange()
    data class RecordChanged(val record: Record, val syncStatus: SyncStatus, val changeKind: ChangeKind?) : RecordsStatePartialChange()
    data class SyncStatusChanged(val recordId: Long, val syncStatus: SyncStatus, val changeKind: ChangeKind?) : RecordsStatePartialChange()
    data class RecordCreated(val record: Record, val localId: Long?, val syncStatus: SyncStatus, val changeKind: ChangeKind?) : RecordsStatePartialChange()
    data class RecordDeletedCompletely(val recordId: Long) : RecordsStatePartialChange()
}