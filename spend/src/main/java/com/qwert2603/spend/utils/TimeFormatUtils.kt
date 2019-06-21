package com.qwert2603.spend.utils

import android.content.res.Resources
import com.qwert2603.andrlib.util.Const
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.Interval

fun Resources.formatTime(time: Interval): String = formatTime(timeInSeconds = time.minutes() * Const.SECONDS_PER_MINUTE)

fun Resources.formatTime(timeInSeconds: Int): String {
    if (timeInSeconds == 0) return getQuantityString(R.plurals.seconds, 0, 0)
    val seconds = timeInSeconds % Const.SECONDS_PER_MINUTE
    val minutes = timeInSeconds / Const.SECONDS_PER_MINUTE % Const.MINUTES_PER_HOUR
    val hours = timeInSeconds / Const.SECONDS_PER_MINUTE / Const.MINUTES_PER_HOUR % Const.HOURS_PER_DAY
    val days = timeInSeconds / Const.SECONDS_PER_MINUTE / Const.MINUTES_PER_HOUR / Const.HOURS_PER_DAY

    val daysString = days.takeIf { it > 0 }?.let { getQuantityString(R.plurals.days, it, it) }
    val hoursString = hours.takeIf { it > 0 }?.let { getQuantityString(R.plurals.hours, it, it) }
    val minutesString = minutes.takeIf { it > 0 }?.let { getQuantityString(R.plurals.minutes, it, it) }
    val secondsString = seconds.takeIf { it > 0 }?.let { getQuantityString(R.plurals.seconds, it, it) }

    val andText = getString(R.string.and_text).let { " $it " }
    val commaText = getString(R.string.comma_text).let { "$it " }

    val list = listOfNotNull(daysString, hoursString, minutesString, secondsString)
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