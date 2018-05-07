package com.qwert2603.spenddemo.utils

import android.content.res.Resources
import com.qwert2603.spenddemo.R
import java.util.*
import com.qwert2603.andrlib.util.Const as LibConst

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


fun Date.plusDays(days: Int) = Date(this.time + days * com.qwert2603.andrlib.util.Const.MILLIS_PER_DAY)

fun Date.isToday() = this.onlyDate() == Date().onlyDate()
fun Date.isYesterday() = this.onlyDate() == Date(System.currentTimeMillis() - LibConst.MILLIS_PER_DAY).onlyDate()
fun Date.isTomorrow() = this.onlyDate() == Date(System.currentTimeMillis() + LibConst.MILLIS_PER_DAY).onlyDate()

fun Date.toFormattedString(resources: Resources): String = when {
    isToday() -> resources.getString(R.string.today_text)
    isYesterday() -> resources.getString(R.string.yesterday_text)
    isTomorrow() -> resources.getString(R.string.tomorrow_text)
    else -> Const.DATE_FORMAT.format(this)
}

infix operator fun Date.plus(millis: Long) = Date(time + millis)
infix operator fun Date.minus(millis: Long) = this + -millis

val Int.days get() = this * LibConst.MILLIS_PER_DAY