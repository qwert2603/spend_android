package com.qwert2603.syncprocessor.entity

data class Change<out I : Any>(
        val itemId: I,
        val changeKind: ChangeKind
)