package com.qwert2603.syncprocessor.logger

class DefaultLogger:Logger{
    override fun d(tag: String, msg: String) {
        println("$tag: $msg")
    }

    override fun e(tag: String, msg: String, t: Throwable) {
        println("$tag: $msg")
        t.printStackTrace()
    }
}