package com.qwert2603.syncprocessor.datasource

interface LastUpdateRepo {
    fun getLastUpdate(): Long
    fun saveLastUpdate(millis: Long)
}