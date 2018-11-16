package com.qwert2603.spenddemo.model.entity

import com.qwert2603.andrlib.model.IdentifiableLong

/*
todo:
показывать в списке разделители:
* "последние 5 минут"
* "последние 30 дней"
 */
interface RecordsListItem : IdentifiableLong {

    companion object {
        private const val DATE_MULTIPLIER = 10L * 100 * 100 * 100

        val COMPARE_ORDER = { r1: RecordsListItem, r2: RecordsListItem ->
            when {
                r1.dateTime() != r2.dateTime() -> r2.dateTime().compareTo(r1.dateTime())
                r1.priority() != r2.priority() -> r2.priority().compareTo(r1.priority())
                else -> r2.idInList().compareTo(r1.idInList())
            }
        }
    }

    // format is "yyyyMMdd0HHmm"; (date * DATE_MULTIPLIER) + time
    fun dateTime() = when (this) {
        is Record -> date * DATE_MULTIPLIER + (time ?: 0)
        is DaySum -> day * DATE_MULTIPLIER * (100L * 100L)
        is MonthSum -> (month * 100L) * DATE_MULTIPLIER * (100L * 100L)
        is YearSum -> (year * 100L * 100L) * DATE_MULTIPLIER * (100L * 100L)
        is Totals -> Long.MIN_VALUE
        else -> null!!
    }

    fun priority() = when (this) {
        is Record -> 5
        is DaySum -> 4
        is MonthSum -> 3
        is YearSum -> 2
        is Totals -> 1
        else -> null!!
    }

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun idInList(): Comparable<Any> = when (this) {
        is Record -> uuid
        is DaySum -> day + 1_000_000_000_000L
        is MonthSum -> month + 2_000_000_000_000L
        is YearSum -> year + 3_000_000_000_000L
        is Totals -> id
        else -> null!!
    } as Comparable<Any>
}