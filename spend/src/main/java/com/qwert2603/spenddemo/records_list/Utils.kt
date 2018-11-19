package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.DaySum
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordsListItem
import com.qwert2603.spenddemo.model.entity.Totals
import com.qwert2603.spenddemo.model.sync_processor.IdentifiableString
import com.qwert2603.spenddemo.utils.Const

private val FAKE_RECORD = Record(
        uuid = IdentifiableString.NO_UUID,
        recordTypeId = Const.RECORD_TYPE_ID_SPEND,
        date = 0,
        time = null,
        kind = "nth",
        value = 1,
        change = null
)

fun List<Record>.toRecordItemsList(showInfo: ShowInfo): List<RecordsListItem> {
    val currentTimeMillis = System.currentTimeMillis()

    var spendsCount = 0
    var spendsSum = 0L
    var profitsCount = 0
    var profitsSum = 0L

    var daySpendsSum = 0L
    var dayProfitsSum = 0L
    var daySpendsCount = 0
    var dayProfitsCount = 0

    val result = ArrayList<RecordsListItem>(this.size * 2 + 1)
    (0..this.lastIndex + 1).forEach { index ->
        // FAKE_RECORD is needed to add DaySum for earliest real RecordResult in list.
        val record = this.getOrNull(index) ?: FAKE_RECORD
        if (index > 0 && showInfo.showSums) {
            if (this[index - 1].date != record.date
                    && (showInfo.showSpends && daySpendsCount > 0 || showInfo.showProfits && dayProfitsCount > 0)
            ) {
                result.add(DaySum(
                        day = this[index - 1].date,
                        showSpends = showInfo.showSpends,
                        showProfits = showInfo.showProfits,
                        spends = daySpendsSum,
                        profits = dayProfitsSum
                ))
                daySpendsSum = 0L
                dayProfitsSum = 0L
                daySpendsCount = 0
                dayProfitsCount = 0
            }
        }
        when {
            record == FAKE_RECORD -> {
                // nth
            }
            record.recordTypeId == Const.RECORD_TYPE_ID_SPEND -> {
                if (record.change?.changeKindId != Const.CHANGE_KIND_DELETE) {
                    daySpendsSum += record.value
                    ++spendsCount
                    spendsSum += record.value
                }
                if (showInfo.showSpends && (record.change?.changeKindId != Const.CHANGE_KIND_DELETE || showInfo.showDeleted())) {
                    result.add(record)
                }
                if (record.change?.changeKindId != Const.CHANGE_KIND_DELETE || showInfo.showDeleted()) {
                    ++daySpendsCount
                }
            }
            record.recordTypeId == Const.RECORD_TYPE_ID_PROFIT -> {
                if (record.change?.changeKindId != Const.CHANGE_KIND_DELETE) {
                    dayProfitsSum += record.value
                    ++profitsCount
                    profitsSum += record.value
                }
                if (showInfo.showProfits && (record.change?.changeKindId != Const.CHANGE_KIND_DELETE || showInfo.showDeleted())) {
                    result.add(record)
                }
                if (record.change?.changeKindId != Const.CHANGE_KIND_DELETE || showInfo.showDeleted()) {
                    ++dayProfitsCount
                }
            }
        }
    }
    result.add(Totals(
            showSpends = showInfo.showSpends,
            showProfits = showInfo.showProfits,
            spendsCount = spendsCount,
            spendsSum = spendsSum,
            profitsCount = profitsCount,
            profitsSum = profitsSum,
            totalBalance = profitsSum - spendsSum
    ))

    LogUtils.d("timing_ List<Record>.toRecordItemsList() ${System.currentTimeMillis() - currentTimeMillis} ms")

    return result
}