package com.qwert2603.spenddemo.model.syncprocessor

import com.qwert2603.spenddemo.utils.LogUtils
import com.qwert2603.syncprocessor.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggerImpl @Inject constructor() : Logger {
    override fun d(tag: String, msg: String) {
        LogUtils.d(tag, msg)
    }

    override fun e(tag: String, msg: String, t: Throwable?) {
        LogUtils.e(tag, msg, t)
    }
}