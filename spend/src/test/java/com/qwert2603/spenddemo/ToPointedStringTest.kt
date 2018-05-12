package com.qwert2603.spenddemo

import com.qwert2603.spenddemo.utils.toPointedString
import org.junit.Assert
import org.junit.Test

class ToPointedStringTest {
    @Test
    fun f() {
        Assert.assertEquals("-2", (-2L).toPointedString())
        Assert.assertEquals("2", (2L).toPointedString())
        Assert.assertEquals("12", 12L.toPointedString())
        Assert.assertEquals("-12", (-12L).toPointedString())
        Assert.assertEquals("0", 0L.toPointedString())
        Assert.assertEquals("234", (234L).toPointedString())
        Assert.assertEquals("1.234", (1234L).toPointedString())
        Assert.assertEquals("-1.234", (-1234L).toPointedString())
        Assert.assertEquals("-234", (-234L).toPointedString())
        Assert.assertEquals("-1.234.567", (-1234567L).toPointedString())
        Assert.assertEquals("-234.567", (-234567L).toPointedString())
    }

    @Test
    fun r_2() {
        for (i in 1..10_000_000) (-1234567L).toPointedString()
    }
}