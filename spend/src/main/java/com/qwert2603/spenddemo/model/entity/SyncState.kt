package com.qwert2603.spenddemo.model.entity

sealed class SyncState(val indicator: String) {
    object Syncing : SyncState("..")
    data class Synced(val updatedRecordsCount: Int) : SyncState("")
    data class Error(val t: Throwable) : SyncState(" X")
}