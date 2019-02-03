package com.qwert2603.spend.model.sync_processor

import com.qwert2603.spend.model.rest.entity.LastChangeInfo

interface LastChangeStorage {
    var lastChangeInfo: LastChangeInfo?
}