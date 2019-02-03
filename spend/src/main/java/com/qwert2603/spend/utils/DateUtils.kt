package com.qwert2603.spend.utils

import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime
import com.qwert2603.spend.model.entity.toSDate
import com.qwert2603.spend.model.entity.toSTime
import java.util.*

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

object DateUtils {
    fun getNow(): Pair<SDate, STime> {
        val calendar = Calendar.getInstance()
        return calendar.toSDate() to calendar.toSTime()
    }
}