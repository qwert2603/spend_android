package com.qwert2603.spenddemo

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.utils.FastDiffUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FastDiffUtilsTest {

    @Before
    fun before() {
        LogUtils.logType = LogUtils.LogType.SOUT
    }

    @Test
    fun removes() {
        val fastDiffResult = FastDiffUtils.fastCalculateDiff(
                oldList = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                newList = listOf(1, 2, 4, 7, 8),
                id = { this },
                compareOrder = Int::compareTo,
                isEqual = Int::equals
        )

        Assert.assertEquals(
                FastDiffUtils.FastDiffResult(
                        removes = listOf(
                                0 to 1,
                                2 to 1,
                                3 to 2,
                                5 to 1
                        ),
                        inserts = emptyList(),
                        changes = emptyList()
                ),
                fastDiffResult
        )
    }

    @Test
    fun inserts() {
        val fastDiffResult = FastDiffUtils.fastCalculateDiff(
                oldList = listOf(1, 2, 4, 7, 8),
                newList = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                id = { this },
                compareOrder = Int::compareTo,
                isEqual = Int::equals
        )

        Assert.assertEquals(
                FastDiffUtils.FastDiffResult(
                        removes = emptyList(),
                        inserts = listOf(
                                0 to 1,
                                3 to 1,
                                5 to 2,
                                9 to 1
                        ),
                        changes = emptyList()
                ),
                fastDiffResult
        )
    }

    @Test
    fun `removes & inserts all`() {
        val fastDiffResult = FastDiffUtils.fastCalculateDiff(
                oldList = listOf(1, 3, 5, 7),
                newList = listOf(0, 2, 4, 6),
                id = { this },
                compareOrder = Int::compareTo,
                isEqual = Int::equals
        )

        Assert.assertEquals(
                FastDiffUtils.FastDiffResult(
                        removes = listOf(
                                0 to 1,
                                0 to 1,
                                0 to 1,
                                0 to 1
                        ),
                        inserts = listOf(
                                0 to 1,
                                1 to 1,
                                2 to 1,
                                3 to 1
                        ),
                        changes = emptyList()
                ),
                fastDiffResult
        )
    }

    @Test
    fun `removes & inserts`() {
        val fastDiffResult = FastDiffUtils.fastCalculateDiff(
                oldList = listOf(0, 1, 3, 4, 6, 7, 8),
                newList = listOf(1, 2, 3, 4, 6, 7, 9),
                id = { this },
                compareOrder = Int::compareTo,
                isEqual = Int::equals
        )

        Assert.assertEquals(
                FastDiffUtils.FastDiffResult(
                        removes = listOf(
                                0 to 1,
                                5 to 1
                        ),
                        inserts = listOf(
                                1 to 1,
                                6 to 1
                        ),
                        changes = emptyList()
                ),
                fastDiffResult
        )
    }

    @Test
    fun `removes & inserts with (fake) move`() {
        val fastDiffResult = FastDiffUtils.fastCalculateDiff(
                oldList = listOf(0, 1, 4, 3, 6, 7, 8),
                newList = listOf(1, 2, 3, 4, 6, 7, 9),
                id = { this },
                compareOrder = Int::compareTo,
                isEqual = Int::equals
        )

        Assert.assertEquals(
                FastDiffUtils.FastDiffResult(
                        removes = listOf(
                                0 to 1,
                                2 to 1,
                                4 to 1
                        ),
                        inserts = listOf(
                                1 to 2,
                                6 to 1
                        ),
                        changes = emptyList()
                ),
                fastDiffResult
        )
    }

    @Test
    fun `removes & inserts with changes`() {
        val fastDiffResult = FastDiffUtils.fastCalculateDiff(
                oldList = listOf<Number>(0, 1, 3, 4, 6, 7L, 8),
                newList = listOf<Number>(1L, 2, 3, 4, 6, 7, 9),
                id = {
                    when (this) {
                        is Int -> this
                        is Long -> this.toInt()
                        else -> null!!
                    }
                },
                compareOrder = { q1, q2 -> q1.toLong().compareTo(q2.toLong()) },
                isEqual = { q1, q2 -> (q1.javaClass == q2.javaClass && q1 == q2) }
        )

        Assert.assertEquals(
                FastDiffUtils.FastDiffResult(
                        removes = listOf(
                                0 to 1,
                                5 to 1
                        ),
                        inserts = listOf(
                                1 to 1,
                                6 to 1
                        ),
                        changes = listOf(
                                1,
                                5
                        )
                ),
                fastDiffResult
        )
    }

}