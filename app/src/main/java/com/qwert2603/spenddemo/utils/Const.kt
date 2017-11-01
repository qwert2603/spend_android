package com.qwert2603.spenddemo.utils

import java.text.SimpleDateFormat
import java.util.*

object Const {
    const val HOURS_PER_DAY = 24
    const val MINUTES_PER_HOUR = 60
    const val SECONDS_PER_MINUTE = 60
    const val MILLIS_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * 1000L
    const val DAYS_PER_WEEK = 7

    val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
}