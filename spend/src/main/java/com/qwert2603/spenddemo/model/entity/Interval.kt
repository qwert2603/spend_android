package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.util.Const

sealed class Interval {
    abstract fun minutes(): Long
}


data class Days(val days: Int) : Interval() {
    override fun minutes() = days.toLong() * Const.MINUTES_PER_DAY
}

val Int.days: Days get() = Days(this)
operator fun Days.unaryMinus() = Days(-days)


data class Minutes(val minutes: Int) : Interval() {
    override fun minutes() = minutes.toLong()
}

val Int.minutes: Minutes get() = Minutes(this)
operator fun Minutes.unaryMinus() = Minutes(-minutes)