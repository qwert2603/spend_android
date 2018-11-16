package com.qwert2603.spenddemo.utils

import android.content.res.Resources
import android.support.annotation.MainThread
import com.qwert2603.spenddemo.R
import java.util.*
import com.qwert2603.andrlib.util.Const as LibConst

fun java.util.Date.toSqlDate() = java.sql.Date(this.time)
fun java.util.Date.toSqlTime() = java.sql.Time(this.time)

fun Date.onlyDate(): Date = Calendar
        .getInstance()
        .also { it.time = this }
        .also {
            it.hour = 0
            it.minute = 0
            it.second = 0
            it.millisecond = 0
        }
        .time

fun Date.onlyTime(): Date = Calendar
        .getInstance()
        .also { it.time = this }
        .also {
            it.year = Const.MIN_YEAR
            it.month = 0
            it.day = 1
            it.second = 0
            it.millisecond = 0
        }
        .time

fun Int.isToday() = this == Calendar.getInstance().toDateInt()
fun Int.isYesterday() = this == Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -1) }.toDateInt()
fun Int.isTomorrow() = this == Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, 1) }.toDateInt()

@MainThread
fun Int.toFormattedDateString(resources: Resources): String = when {
    isToday() -> resources.getString(R.string.today_text)
    isYesterday() -> resources.getString(R.string.yesterday_text)
    isTomorrow() -> resources.getString(R.string.tomorrow_text)
    else -> this.toDateString()
}

infix operator fun Date.plus(millis: Long) = Date(time + millis)
infix operator fun Date.minus(millis: Long) = this + -millis

val Int.days get() = this * LibConst.MILLIS_PER_DAY
val Int.minutes get() = this * LibConst.MILLIS_PER_MINUTE

fun Calendar.minutesEqual(anth: Calendar) = listOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR, Calendar.MINUTE)
        .all { this[it] == anth[it] }

fun Calendar.daysEqual(anth: Calendar) = this[Calendar.YEAR] == anth[Calendar.YEAR] && this[Calendar.DAY_OF_YEAR] == anth[Calendar.DAY_OF_YEAR]
fun Calendar.monthsEqual(anth: Calendar) = this[Calendar.YEAR] == anth[Calendar.YEAR] && this[Calendar.MONTH] == anth[Calendar.MONTH]

fun Calendar.onlyDate(): Date = GregorianCalendar(this[Calendar.YEAR], this[Calendar.MONTH], this[Calendar.DAY_OF_MONTH]).time
fun Calendar.onlyMonth(): Date = GregorianCalendar(this[Calendar.YEAR], this[Calendar.MONTH], 1).time

var Calendar.year
    get() = this[Calendar.YEAR]
    set(value) {
        this[Calendar.YEAR] = value
    }

var Calendar.month
    get() = this[Calendar.MONTH]
    set(value) {
        this[Calendar.MONTH] = value
    }

var Calendar.day
    get() = this[Calendar.DAY_OF_MONTH]
    set(value) {
        this[Calendar.DAY_OF_MONTH] = value
    }

var Calendar.hour
    get() = this[Calendar.HOUR_OF_DAY]
    set(value) {
        this[Calendar.HOUR_OF_DAY] = value
    }

var Calendar.minute
    get() = this[Calendar.MINUTE]
    set(value) {
        this[Calendar.MINUTE] = value
    }

var Calendar.second
    get() = this[Calendar.SECOND]
    set(value) {
        this[Calendar.SECOND] = value
    }

var Calendar.millisecond
    get() = this[Calendar.MILLISECOND]
    set(value) {
        this[Calendar.MILLISECOND] = value
    }

fun Date.secondsToZero(): Date = Calendar.getInstance()
        .also {
            it.time = this
            it.second = 0
            it.millisecond = 0
        }
        .time


object DateUtils {
    fun getNow(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(
                calendar.year * 10000 + (calendar.month + 1) * 100 + calendar.day,
                calendar.hour * 100 + calendar.minute
        )
    }
}

fun Int.toDateString() = String.format("%04d-%02d-%02d", this / (100 * 100), this / 100 % 100, this % 100)
fun Int.toTimeString() = String.format("%d:%02d", this / 100, this % 100)

fun Calendar.toDateInt() = year * (100 * 100) + (month + 1) * 100 + day
fun Calendar.toTimeInt() = hour * 100 + minute

fun Int.toDateCalendar() = GregorianCalendar(this / (100 * 100), (this / 100 % 100) - 1, this % 100)
fun Int.toTimeCalendar() = GregorianCalendar(1970, Calendar.JANUARY, 1, this / 100, this % 100, 0)