package com.qwert2603.spenddemo.utils

import android.content.res.Resources
import com.qwert2603.spenddemo.R
import java.text.SimpleDateFormat
import java.util.*
import com.qwert2603.andrlib.util.Const as LibConst

fun java.util.Date.toSqlDate() = java.sql.Date(this.time)
fun java.util.Date.toSqlTimestamp() = java.sql.Timestamp(this.time)

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

fun Date.onlyMonth(): Date = Calendar
        .getInstance()
        .also { it.time = this.onlyDate() }
        .also { it.set(Calendar.DAY_OF_MONTH, 1) }
        .time

fun Date.plusDays(days: Int) = Date(this.time + days * com.qwert2603.andrlib.util.Const.MILLIS_PER_DAY)

fun Date.isToday() = this.onlyDate() == Date().onlyDate()
fun Date.isYesterday() = this.onlyDate() == Date(System.currentTimeMillis() - LibConst.MILLIS_PER_DAY).onlyDate()
fun Date.isTomorrow() = this.onlyDate() == Date(System.currentTimeMillis() + LibConst.MILLIS_PER_DAY).onlyDate()

private val dateFormat = SimpleDateFormat(Const.DATE_FORMAT_PATTERN, Locale.getDefault())
fun Date.toFormattedString(resources: Resources): String = when {
    isToday() -> resources.getString(R.string.today_text)
    isYesterday() -> resources.getString(R.string.yesterday_text)
    isTomorrow() -> resources.getString(R.string.tomorrow_text)
    else -> dateFormat.format(this)
}

infix operator fun Date.plus(millis: Long) = Date(time + millis)
infix operator fun Date.minus(millis: Long) = this + -millis

val Int.days get() = this * LibConst.MILLIS_PER_DAY

fun Calendar.daysEqual(anth: Calendar) = this[Calendar.YEAR] == anth.get(Calendar.YEAR) && this[Calendar.DAY_OF_YEAR] == anth[Calendar.DAY_OF_YEAR]
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

fun Date.setDayFrom(date: Date): Date {
    val calendar = Calendar.getInstance()
            .also { it.timeInMillis = this.time }
    val anth = Calendar.getInstance()
            .also { it.timeInMillis = date.time }
    calendar.year = anth.year
    calendar.month = anth.month
    calendar.day = anth.day
    this.time = calendar.timeInMillis
    return this
}

fun Date.setTimeFrom(date: Date): Date {
    val calendar = Calendar.getInstance()
            .also { it.timeInMillis = this.time }
    val anth = Calendar.getInstance()
            .also { it.timeInMillis = date.time }
    calendar.hour = anth.hour
    calendar.minute = anth.minute
    this.time = calendar.timeInMillis
    return this
}

fun Date.secondsToZero(): Date = Calendar.getInstance()
        .also {
            it.time = this
            it.set(Calendar.SECOND, 0)
            it.set(Calendar.MILLISECOND, 0)
        }
        .time