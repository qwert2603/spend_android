package com.qwert2603.spend.model.entity

import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.spend.records_list.RecordsListAdapter
import com.qwert2603.spend.records_list.RecordsListViewState

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

        val IS_EQUAL = { r1: RecordsListItem, r2: RecordsListItem ->
            if (r1 is Record && r2 is Record) {
                /**
                 * difference in [Record.change] is consumed via
                 * [RecordsListViewState.recordsChanges] and [RecordsListAdapter.recordsChanges].
                 */
                r1.equalIgnoreChange(r2)
            } else if (r1 is PeriodDivider && r2 is PeriodDivider) {
                r1.interval == r2.interval
            } else {
                r1 == r2
            }
        }
    }

    // format is "yyyyMMdd0HHmm"; (date * DATE_MULTIPLIER) + time
    fun dateTime(): Long = when (this) {
        is Record -> date.date * DATE_MULTIPLIER + if (time != null) time.time + 100 * 100 else 0
        is DaySum -> day.date * DATE_MULTIPLIER
        is MonthSum -> (month * 100L + 1) * DATE_MULTIPLIER
        is YearSum -> (year * 100L * 100L + 101) * DATE_MULTIPLIER
        is PeriodDivider -> date.date * DATE_MULTIPLIER + if (time != null) time.time + 100 * 100 else 0
        is Totals -> SDate.MIN_VALUE.date * DATE_MULTIPLIER
        else -> null!!
    }

    fun priority() = when (this) {
        is Record -> 6
        is DaySum -> 5
        is MonthSum -> 4
        is YearSum -> 3
        is PeriodDivider -> 2
        is Totals -> 1
        else -> null!!
    }

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun idInList(): Comparable<Any> = when (this) {
        is Record -> uuid
        is DaySum -> day.date + 1 * ID_IN_LIST_MULTIPLIER
        is MonthSum -> month + 2 * ID_IN_LIST_MULTIPLIER
        is YearSum -> year + 3 * ID_IN_LIST_MULTIPLIER
        is PeriodDivider -> interval.minutes() + 4 * ID_IN_LIST_MULTIPLIER
        is Totals -> id
        else -> null!!
    } as Comparable<Any>

    fun date(): SDate = (dateTime() / DATE_MULTIPLIER).toInt().toSDate()
}