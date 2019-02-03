package com.qwert2603.spend

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.model.repo_impl.RecordAggregationsRepoImpl
import com.qwert2603.spend.utils.Const
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.*
import kotlin.random.Random

class AggregateRecordsTest {

    @Before
    fun before() {
        LogUtils.logType = LogUtils.LogType.SOUT
    }

    @Test
    fun empty() {
        val actual = RecordAggregationsRepoImpl.aggregate(emptyList(), emptyList())
        Assert.assertEquals(
                RecordAggregationsRepoImpl.AggregationResult(
                        recordsCategoriesList = mapOf(
                                Const.RECORD_TYPE_ID_SPEND to emptyList(),
                                Const.RECORD_TYPE_ID_PROFIT to emptyList()
                        ),
                        recordsKindsLists = mapOf(
                                Const.RECORD_TYPE_ID_SPEND to mapOf<String?, List<RecordKindAggregation>>(null to emptyList()),
                                Const.RECORD_TYPE_ID_PROFIT to mapOf<String?, List<RecordKindAggregation>>(null to emptyList())
                        )
                ),
                actual
        )
    }

    @Test
    fun test() {
        val c1 = RecordCategory("u_c1", Const.RECORD_TYPE_ID_SPEND, "n_c1")
        val c2 = RecordCategory("u_c2", Const.RECORD_TYPE_ID_SPEND, "n_c2")
        val c3 = RecordCategory("u_c3", Const.RECORD_TYPE_ID_SPEND, "n_c_s")
        val c4 = RecordCategory("u_c4", Const.RECORD_TYPE_ID_PROFIT, "n_c3")
        val c5 = RecordCategory("u_c5", Const.RECORD_TYPE_ID_PROFIT, "n_c5")
        val c6 = RecordCategory("u_c6", Const.RECORD_TYPE_ID_PROFIT, "n_c_s")

        val r1 = Record("u_r1", c3, SDate(20181203), STime(1918), "k1", 1, null)
        val r2 = Record("u_r2", c6, SDate(20181203), STime(1918), "k1", 2, null)

        val r3 = Record("u_r3", c1, SDate(20181203), STime(1417), "k1", 3, RecordChange(1, false))
        val r4 = Record("u_r4", c1, SDate(20181203), STime(1918), "k1", 4, null)
        val r5 = Record("u_r5", c1, SDate(20181203), STime(2310), "k2", 5, RecordChange(2, false))
        val r6 = Record("u_r6", c2, SDate(20181204), null, "k1", 6, RecordChange(3, false))
        val r7 = Record("u_r7", c4, SDate(20181203), STime(1918), "k1", 7, null)
        val r8 = Record("u_r7", c4, SDate(20181203), STime(1918), "k1", 7, RecordChange(101, true))
        val r9 = Record("u_r7", c4, SDate(20181203), STime(1918), "k1", 7, RecordChange(102, true))
        val r10 = Record("u_r7", c4, SDate(20181203), STime(1918), "k1", 7, RecordChange(103, true))

        val expected = RecordAggregationsRepoImpl.AggregationResult(
                recordsCategoriesList = mapOf(
                        Const.RECORD_TYPE_ID_SPEND to listOf(
                                RecordCategoryAggregation(Const.RECORD_TYPE_ID_SPEND, c1, r5, 3, 12),
                                RecordCategoryAggregation(Const.RECORD_TYPE_ID_SPEND, c2, r6, 1, 6),
                                RecordCategoryAggregation(Const.RECORD_TYPE_ID_SPEND, c3, r1, 1, 1)
                        ),
                        Const.RECORD_TYPE_ID_PROFIT to listOf(
                                RecordCategoryAggregation(Const.RECORD_TYPE_ID_PROFIT, c4, r7, 1, 7),
                                RecordCategoryAggregation(Const.RECORD_TYPE_ID_PROFIT, c6, r2, 1, 2),
                                RecordCategoryAggregation(Const.RECORD_TYPE_ID_PROFIT, c5, null, 0, 0)
                        )
                ),
                recordsKindsLists = mapOf(
                        Const.RECORD_TYPE_ID_SPEND to mapOf(
                                c1.uuid to listOf(
                                        RecordKindAggregation(Const.RECORD_TYPE_ID_SPEND, c1, r4.kind, r4, 2, 7),
                                        RecordKindAggregation(Const.RECORD_TYPE_ID_SPEND, c1, r5.kind, r5, 1, 5)
                                ),
                                c2.uuid to listOf(RecordKindAggregation(Const.RECORD_TYPE_ID_SPEND, c2, r6.kind, r6, 1, 6)),
                                c3.uuid to listOf(RecordKindAggregation(Const.RECORD_TYPE_ID_SPEND, c3, r1.kind, r1, 1, 1)),
                                null to listOf(
                                        RecordKindAggregation(Const.RECORD_TYPE_ID_SPEND, c1, r4.kind, r4, 2, 7),
                                        RecordKindAggregation(Const.RECORD_TYPE_ID_SPEND, c2, r6.kind, r6, 1, 6),
                                        RecordKindAggregation(Const.RECORD_TYPE_ID_SPEND, c1, r5.kind, r5, 1, 5),
                                        RecordKindAggregation(Const.RECORD_TYPE_ID_SPEND, c3, r1.kind, r1, 1, 1)
                                )
                        ),
                        Const.RECORD_TYPE_ID_PROFIT to mapOf(
                                c4.uuid to listOf(RecordKindAggregation(Const.RECORD_TYPE_ID_PROFIT, c4, r7.kind, r7, 1, 7)),
                                c5.uuid to emptyList(),
                                c6.uuid to listOf(RecordKindAggregation(Const.RECORD_TYPE_ID_PROFIT, c6, r2.kind, r2, 1, 2)),
                                null to listOf(
                                        RecordKindAggregation(Const.RECORD_TYPE_ID_PROFIT, c4, r7.kind, r7, 1, 7),
                                        RecordKindAggregation(Const.RECORD_TYPE_ID_PROFIT, c6, r2.kind, r2, 1, 2)
                                )
                        )
                )
        )

        val actual = RecordAggregationsRepoImpl.aggregate(
                records = listOf(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10),
                categories = listOf(c1, c2, c3, c4, c5, c6)
        )

        fun <T> makeAssert(extractor: (RecordAggregationsRepoImpl.AggregationResult) -> T) = Assert.assertEquals(extractor(expected), extractor(actual))

        makeAssert { it.recordsCategoriesList[Const.RECORD_TYPE_ID_SPEND] }
        makeAssert { it.recordsCategoriesList[Const.RECORD_TYPE_ID_PROFIT] }
        makeAssert { it.recordsCategoriesList }
        makeAssert { it.recordsKindsLists[Const.RECORD_TYPE_ID_SPEND] }
        makeAssert { it.recordsKindsLists[Const.RECORD_TYPE_ID_PROFIT] }
        makeAssert { it.recordsKindsLists }
        makeAssert { it }
    }

    @Test
    @Ignore
    fun stress() {
        val categories = (0..1000).map {
            RecordCategory(
                    UUID.randomUUID().toString(),
                    if (Random.nextBoolean()) Const.RECORD_TYPE_ID_SPEND else Const.RECORD_TYPE_ID_PROFIT,
                    randomName()
            )
        }
        val kinds = (0..500).map { randomName() }
        val records = (0..100_000).map {
            Record(
                    UUID.randomUUID().toString(),
                    categories.random(),
                    SDate(3),
                    STime(3).takeIf { Random.nextBoolean() },
                    kinds.random(),
                    Random.nextInt(1, 10000),
                    null
            )
        }

        repeat(10) { RecordAggregationsRepoImpl.aggregate(records, categories) }
    }

    private fun randomName() = Random
            .nextInt(6, 15)
            .let { 0 until it }
            .map { Random.nextInt(26) }
            .map { 'a' + it }
            .toCharArray()
            .let { String(it) }
}