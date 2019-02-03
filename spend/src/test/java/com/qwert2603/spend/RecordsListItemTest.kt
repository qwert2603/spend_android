package com.qwert2603.spend

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.utils.Const
import org.junit.Assert
import org.junit.Before
import org.junit.Test

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
}