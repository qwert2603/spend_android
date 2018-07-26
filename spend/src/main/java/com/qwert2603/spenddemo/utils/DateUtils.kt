package com.qwert2603.spenddemo.utils

import android.content.res.Resources
import com.qwert2603.spenddemo.R
import java.text.SimpleDateFormat
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

fun Date.isToday() = this.onlyDate() == Date().onlyDate()
fun Date.isYesterday() = this.onlyDate() == Date().onlyDate() - 1.days
fun Date.isTomorrow() = this.onlyDate() == Date().onlyDate() + 1.days

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
val Int.minutes get() = this * LibConst.MILLIS_PER_MINUTE

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