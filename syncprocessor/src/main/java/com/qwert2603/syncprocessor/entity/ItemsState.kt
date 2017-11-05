package com.qwert2603.syncprocessor.entity

data class ItemsState<I, out T : Identifiable<I>>(
        val items: List<T>,
        val changes: Map<I, ChangeKind>,
        val syncingItems: Set<I>
)