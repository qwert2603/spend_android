package com.qwert2603.spenddemo.model.sync_processor

interface IdCounter {
    fun getNext(): Long
}