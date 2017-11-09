package com.qwert2603.syncprocessor.stub

import com.qwert2603.syncprocessor.datasource.LastUpdateRepo

class StubLastUpdateRepo : LastUpdateRepo {
    override fun getLastUpdate() = 0L
    override fun saveLastUpdate(millis: Long) {}
}