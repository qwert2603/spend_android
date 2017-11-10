package com.qwert2603.spenddemo.model.entity

import com.qwert2603.syncprocessor.entity.ChangeKind as SyncChangeKind

enum class ChangeKind {
    INSERT, UPDATE, DELETE
}

fun ChangeKind.toSyncChangeKind() = when(this) {
    ChangeKind.INSERT -> SyncChangeKind.CREATE
    ChangeKind.UPDATE -> SyncChangeKind.EDIT
    ChangeKind.DELETE -> SyncChangeKind.DELETE
}

fun SyncChangeKind.toChangeKind() = when(this) {
    SyncChangeKind.CREATE -> ChangeKind.INSERT
    SyncChangeKind.EDIT -> ChangeKind.UPDATE
    SyncChangeKind.DELETE -> ChangeKind.DELETE
}