package com.qwert2603.spenddemo.model.sync_processor

import java.sql.Timestamp

data class LastUpdateInfo(
        val lastUpdateTimestamp: Timestamp,
        val lastUpdatedId: Long
)