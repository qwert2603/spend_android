package com.qwert2603.spenddemo.utils

interface SuspendMapper<T, U> {
    suspend operator fun invoke(t: T): U
}