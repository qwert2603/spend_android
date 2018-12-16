package com.qwert2603.spenddemo.utils

data class Wrapper<T>(val t: T?)

fun <T> T?.wrap() = Wrapper(this)