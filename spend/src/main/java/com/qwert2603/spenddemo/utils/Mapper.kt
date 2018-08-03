package com.qwert2603.spenddemo.utils

interface Mapper<T, U> {
    suspend operator fun invoke(t: T): U
}