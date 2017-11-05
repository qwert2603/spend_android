package com.qwert2603.syncprocessor.entity

data class Change<out I>(
        val itemId: I,
        val changeKind: ChangeKind
)