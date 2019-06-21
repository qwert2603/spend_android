package com.qwert2603.spend

import com.qwert2603.spend.utils.getLateInitOrNull
import org.junit.Assert
import org.junit.Test

class A {
    lateinit var s: String
}

class GetLateInitOrNullTest {
    @Test(expected = UninitializedPropertyAccessException::class)
    fun test1() {
        val a = A()
        println(a.s)
    }

    @Test
    fun test2() {
        val a = A()
        Assert.assertNull(a::s.getLateInitOrNull())
        Assert.assertNull(a::s.getLateInitOrNull())
        a.s = "smth"
        Assert.assertEquals("smth", a::s.getLateInitOrNull())
        Assert.assertEquals("smth", a::s.getLateInitOrNull())
    }
}