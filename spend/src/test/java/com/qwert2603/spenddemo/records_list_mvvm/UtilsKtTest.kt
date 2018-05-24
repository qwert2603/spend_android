package com.qwert2603.spenddemo.records_list_mvvm

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.utils.days
import com.qwert2603.spenddemo.utils.minus
import org.junit.Before
import org.junit.Test
import java.util.*

class UtilsKtTest {

    private val random = Random()
    private val recordResults = (1..2_000_000)
            .map {
                RecordResult(
                        type = 1 + random.nextInt(2),
                        id = random.nextLong(),
                        kind = "kind",
                        value = 1 + random.nextInt(1000),
                        date = Date() - (random.nextInt(2100)).days
                )
            }
            .sortedByDescending { it.date }

    @Before
    fun before() {
        LogUtils.logType = LogUtils.LogType.SOUT
    }

    @Test
    fun toRecordItemsList() {
        recordResults.toRecordItemsList(true, true, true, true, true, true)
    }

}