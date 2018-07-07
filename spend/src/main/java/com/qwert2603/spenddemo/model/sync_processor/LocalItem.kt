package com.qwert2603.spenddemo.model.sync_processor

import com.qwert2603.spenddemo.model.entity.RecordChange

interface LocalItem {
    val id: Long
    val change: RecordChange?
}