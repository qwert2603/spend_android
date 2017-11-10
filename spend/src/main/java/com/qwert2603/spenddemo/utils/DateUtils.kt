package com.qwert2603.spenddemo.utils

import java.util.*

fun java.util.Date.toSqlDate() = java.sql.Date(this.time)
fun java.sql.Date.toUtilDate() = java.util.Date(this.time)

fun Date.onlyDate(): Date = Calendar
        .getInstance()
        .also { it.time = this }
        .also {
            it.set(Calendar.HOUR_OF_DAY, 0)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            it.set(Calendar.MILLISECOND, 0)
        }
        .time