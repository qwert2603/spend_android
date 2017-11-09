package com.qwert2603.syncprocessor.entity

data class TimedChange(
        val changeKind: ChangeKind,
        val changeMillis: Long?
)