package com.qwert2603.spenddemo.model.entity

import android.content.res.Resources
import android.support.annotation.MainThread
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.utils.day
import com.qwert2603.spenddemo.utils.month
import com.qwert2603.spenddemo.utils.year
import java.util.*

/** format is "yyyyMMdd" */
data class SDate(val date: Int) : Comparable<SDate> {
    override fun toString() = String.format("%04d-%02d-%02d", date / (100 * 100), date / 100 % 100, date % 100)

    fun toDateCalendar() = GregorianCalendar(
            date / (100 * 100),
            (date / 100 % 100) - 1,
            date % 100
    )

    override fun compareTo(other: SDate): Int = this.date.compareTo(other.date)
}

fun Int.toSDate() = SDate(this)

fun Calendar.toSDate() = SDate(year * (100 * 100) + (month + 1) * 100 + day)

fun SDate.isToday() = this == Calendar.getInstance().toSDate()
fun SDate.isYesterday() = this == Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -1) }.toSDate()
fun SDate.isTomorrow() = this == Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, 1) }.toSDate()

@MainThread
fun SDate.toFormattedString(resources: Resources): String = when {
    isToday() -> resources.getString(R.string.today_text)
    isYesterday() -> resources.getString(R.string.yesterday_text)
    isTomorrow() -> resources.getString(R.string.tomorrow_text)
    else -> this.toString()
}