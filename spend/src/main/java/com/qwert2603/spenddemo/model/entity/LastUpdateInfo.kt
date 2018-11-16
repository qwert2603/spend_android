package com.qwert2603.spenddemo.model.entity

data class LastUpdateInfo(
    val lastUpdated: Long, // "updated" of last record sent to client.
    val lastUuid: String // "uuid" of last record sent to client.
)