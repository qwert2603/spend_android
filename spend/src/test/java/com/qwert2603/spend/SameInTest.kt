package com.qwert2603.spend

import com.qwert2603.spend.utils.sameIn
import org.junit.Assert
import org.junit.Test

class SameInTest {
    @Test
    fun test() {
        val map = mapOf("q" to 23, true to 6.44)
        Assert.assertTrue(map sameIn map)

        Assert.assertTrue(mapOf("hh" to 23, true to 6.11) sameIn mapOf("hh" to 23, true to 6.11))

        Assert.assertTrue(emptyMap<Any, Any>() sameIn mapOf("hh" to 23, true to 6.11))

        Assert.assertTrue(mapOf("hh" to 23, true to 6.11) sameIn mapOf("hh" to 23, true to 6.11, "anth" to 55))

        Assert.assertTrue(mapOf("hh" to 23, true to null) sameIn mapOf("hh" to 23, true to null, "anth" to 55))

        Assert.assertTrue(mapOf("hh" to 23, true to null) sameIn mapOf("hh" to 23, "anth" to 55))

        Assert.assertFalse(mapOf("hh" to 23, true to 6.11, "anth" to 55) sameIn mapOf("hh" to 23, true to 6.11))

        Assert.assertFalse(mapOf("hh" to 99, true to 6.11) sameIn mapOf("hh" to 23, true to 6.11))

        Assert.assertFalse(mapOf("hhe" to 23, true to 6.11) sameIn mapOf("hh" to 23, true to 6.11))

        Assert.assertFalse(mapOf("hhe" to 23, true to 6.11) sameIn emptyMap<Any, Any>())
    }
}