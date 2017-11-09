package com.qwert2603.syncprocessor.entity

data class ItemsState<I : Any, out T : Identifiable<I>>(
        val items: List<T>,
        val changes: Map<I, TimedChange>,
        val syncingItems: Set<I>
)