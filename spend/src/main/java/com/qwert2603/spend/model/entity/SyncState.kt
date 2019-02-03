package com.qwert2603.spend.model.entity

sealed class SyncState(val indicator: String) {
    object Syncing : SyncState("..")
    data class Synced(val updatedRecordsCount: Int) : SyncState("")
    data class Error(val t: Throwable) : SyncState(" X")
}