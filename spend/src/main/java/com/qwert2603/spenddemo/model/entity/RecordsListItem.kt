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
        private const val DATE_MULTIPLIER = 10L * 100 * 100

        private val Record.timeNN: Int get() = time?.time ?: -1

        val COMPARE_ORDER = { r1: RecordsListItem, r2: RecordsListItem ->
            if (r1 is Record && r2 is Record) {
                when {
                    r1.date != r2.date -> r1.date.compareTo(r2.date).unaryMinus()
                    r1.timeNN != r2.timeNN -> r1.timeNN.compareTo(r2.timeNN).unaryMinus()
                    r1.recordCategory.recordTypeId != r2.recordCategory.recordTypeId -> r1.recordCategory.recordTypeId.compareTo(r2.recordCategory.recordTypeId)
                    r1.recordCategory.name != r2.recordCategory.name -> r1.recordCategory.name.compareTo(r2.recordCategory.name).unaryMinus()
                    r1.kind != r2.kind -> r1.kind.compareTo(r2.kind).unaryMinus()
                    else -> r1.uuid.compareTo(r2.uuid)
                }
            } else {
                when {
                    r1.dateTime() != r2.dateTime() -> r1.dateTime().compareTo(r2.dateTime()).unaryMinus()
                    r1.priority() != r2.priority() -> r1.priority().compareTo(r2.priority()).unaryMinus()
                    else -> r1.idInList().compareTo(r2.idInList())
                }
            }
        }

        private const val ID_IN_LIST_MULTIPLIER = 1_000_000_000_000L
    }

    // format is "yyyyMMdd0HHmm"; (date * DATE_MULTIPLIER) + time
    fun dateTime() = when (this) {
        is Record -> date.date * DATE_MULTIPLIER + if (time != null) time.time + 100 * 100 else 0
        is DaySum -> day.date * DATE_MULTIPLIER
        is MonthSum -> (month * 100L) * DATE_MULTIPLIER
        is YearSum -> (year * 100L * 100L) * DATE_MULTIPLIER
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
        is DaySum -> day.date + 1 * ID_IN_LIST_MULTIPLIER
        is MonthSum -> month + 2 * ID_IN_LIST_MULTIPLIER
        is YearSum -> year + 3 * ID_IN_LIST_MULTIPLIER
        is Totals -> id
        else -> null!!
    } as Comparable<Any>
}