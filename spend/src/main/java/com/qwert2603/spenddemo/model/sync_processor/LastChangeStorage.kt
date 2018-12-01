package com.qwert2603.spenddemo.model.sync_processor

import com.qwert2603.spenddemo.model.rest.entity.LastChangeInfo

interface LastChangeStorage {
    var lastChangeInfo: LastChangeInfo?
}