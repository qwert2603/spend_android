package com.qwert2603.spenddemo.utils

import android.content.res.Resources
import com.qwert2603.andrlib.util.Const
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.model.entity.Interval

fun Resources.formatTime(time: Interval): String {
    val timeInMinutes = time.minutes()
    if (timeInMinutes == 0) return getQuantityString(R.plurals.minutes, 0, 0)
    val minutes = timeInMinutes % Const.MINUTES_PER_HOUR
    val hours = timeInMinutes / Const.MINUTES_PER_HOUR % Const.HOURS_PER_DAY
    val days = timeInMinutes / Const.MINUTES_PER_HOUR / Const.HOURS_PER_DAY

    val daysString = days.takeIf { it > 0 }?.let { getQuantityString(R.plurals.days, it, it) }
    val hoursString = hours.takeIf { it > 0 }?.let { getQuantityString(R.plurals.hours, it, it) }
    val minutesString = minutes.takeIf { it > 0 }?.let { getQuantityString(R.plurals.minutes, it, it) }

    val andText = getString(R.string.and_text).let { " $it " }
    val commaText = getString(R.string.comma_text).let { "$it " }

    val list = listOfNotNull(daysString, hoursString, minutesString)
    return list
            .reduceIndexed { index, acc, s ->
                val separator = if (index == list.lastIndex) andText else commaText
                "$acc$separator$s"
            }
}

fun Resources.formatTimeLetters(time: Interval): String {
    val timeInMinutes = time.minutes()
    if (timeInMinutes == 0) return "0${getString(R.string.letter_minutes)}"
    val minutes = timeInMinutes % Const.MINUTES_PER_HOUR
    val hours = timeInMinutes / Const.MINUTES_PER_HOUR % Const.HOURS_PER_DAY
    val days = timeInMinutes / Const.MINUTES_PER_HOUR / Const.HOURS_PER_DAY

    val daysString = days.takeIf { it > 0 }?.let { "$it${getString(R.string.letter_days)}" }
    val hoursString = hours.takeIf { it > 0 }?.let { "$it${getString(R.string.letter_hours)}" }
    val minutesString = minutes.takeIf { it > 0 }?.let { "$it${getString(R.string.letter_minutes)}" }

    return listOfNotNull(daysString, hoursString, minutesString)
            .reduce { acc, s -> "$acc$s" }
}