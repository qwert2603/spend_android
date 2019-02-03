package com.qwert2603.spend.utils

data class Wrapper<T>(val t: T?)

fun <T> T?.wrap() = Wrapper(this)