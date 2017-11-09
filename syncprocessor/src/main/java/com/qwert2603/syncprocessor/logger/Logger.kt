package com.qwert2603.syncprocessor.logger

interface Logger {
    fun d(tag: String, msg: String)
    fun e(tag: String, msg: String, t: Throwable?)
}