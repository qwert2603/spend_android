package com.qwert2603.spenddemo.model.entity

enum class SyncState(val indicator: String) {
    SYNCING(".."),
    SYNCED(""),
    ERROR("X");
}