package com.qwert2603.spend

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.records_list.toRecordItemsList
import com.qwert2603.spend.utils.Const
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.random.Random

class RecordsListItemTest {
    @Before
    fun before() {
        LogUtils.logType = LogUtils.LogType.SOUT
    }

    @Test
    fun `Record dateTime`() {
        val c1 = RecordCategory("u_c1", Const.RECORD_TYPE_ID_SPEND, "n_c1")
        val c2 = RecordCategory("u_c2", Const.RECORD_TYPE_ID_SPEND, "n_c2")

        val r1 = Record("u_r5", c1, SDate(20181203), STime(2310), "k2", 5, RecordChange(2, false))
        val r2 = Record("u_r6", c2, SDate(20181204), null, "k1", 6, RecordChange(3, false))

        Assert.assertEquals(20181203_1_2310L, r1.dateTime())
        Assert.assertEquals(20181204_0_0000L, r2.dateTime())
    }

    @Test
    fun `DaySum dateTime`() {
        val d1 = DaySum(SDate(20181203), true, true, 0, 0)
        val d2 = DaySum(SDate(20190101), true, true, 0, 0)

        Assert.assertEquals(20181203_0_0000L, d1.dateTime())
        Assert.assertEquals(20190101_0_0000L, d2.dateTime())
    }

    @Test
    fun `PeriodDivider dateTime`() {
        val p1 = PeriodDivider(SDate(20181203), STime(2310), 30.days)
        val p2 = PeriodDivider(SDate(20181204), null, 5.minutes)

        Assert.assertEquals(20181203_1_2310L, p1.dateTime())
        Assert.assertEquals(20181204_0_0000L, p2.dateTime())
    }

    @Test
    fun `compare order`() {
        val categories = (0..500)
                .map { randomCategory() }

        val recordsListItems = List(100000) { randomRecord(categories) }
                .sortAsFromRepo()
                .toRecordItemsList(
                        showInfo = ShowInfo(true, true, true, true, true),
                        sortByValue = false,
                        showFilters = true,
                        longSumPeriod = Days(182),
                        shortSumPeriod = Minutes(1918),
                        recordsFilters = RecordsFilters("", null, null),
                        selectedRecordsUuids = hashSetOf()
                )

        recordsListItems.zipWithNext { r1, r2 ->
            Assert.assertTrue(RecordsListItem.COMPARE_ORDER(r1, r2) < 0)
        }
    }

    @Test
    fun `compare order with value`() {
        val categories = (0..500)
                .map { randomCategory() }

        val recordsListItems = List(100000) { randomRecord(categories) }
                .sortAsFromRepo()
                .toRecordItemsList(
                        showInfo = ShowInfo(true, true, true, true, true),
                        sortByValue = true,
                        showFilters = true,
                        longSumPeriod = Days(30),
                        shortSumPeriod = Minutes(1918),
                        recordsFilters = RecordsFilters("", null, null),
                        selectedRecordsUuids = hashSetOf()
                )

        recordsListItems.zipWithNext { r1, r2 ->
            Assert.assertTrue(RecordsListItem.COMPARE_ORDER_WITH_VALUE(r1, r2) < 0)
        }
    }

    companion object {

        private val NOW = System.currentTimeMillis()

        private fun randomUuid(): String = UUID.randomUUID().toString()

        private fun randomWord(): String = (1..2)
                .map { 'a' + Random.nextInt(26) }
                .let { String(it.toCharArray()) }

        private fun randomCategory(): RecordCategory {
            return RecordCategory(
                    uuid = randomUuid(),
                    recordTypeId = Random.nextLong(1, 3),
                    name = randomWord()
            )
        }

        private fun randomRecord(categories: List<RecordCategory>): Record {
            val millis = NOW - Random.nextLong(1000L * 60 * 60 * 24 * 90)
            val calendar = Calendar.getInstance().also { it.timeInMillis = millis }
            return Record(
                    uuid = randomUuid(),
                    recordCategory = categories.random(),
                    date = (calendar[Calendar.YEAR] * 10000 + (calendar[Calendar.MONTH] + 1) * 100 + calendar[Calendar.DAY_OF_MONTH]).toSDate(),
                    time = if (Random.nextBoolean()) (calendar[Calendar.HOUR_OF_DAY] * 100 + calendar[Calendar.MINUTE]).toSTime() else null,
                    kind = randomWord(),
                    value = Random.nextInt(1, 10000),
                    change = null
            )
        }

        private fun List<Record>.sortAsFromRepo() = this
                .asSequence()
                .sortedBy { it.uuid }
                .sortedByDescending { it.kind }
                .sortedByDescending { it.recordCategory.name }
                .sortedBy { it.recordCategory.recordTypeId }
                .sortedByDescending { it.time?.time ?: -1 }
                .sortedByDescending { it.date }
                .toList()
    }
}