package com.qwert2603.spend.model.sync_processor

interface IdCounter {
    fun getNext(): Long
}