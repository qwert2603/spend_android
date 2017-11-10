package com.qwert2603.spenddemo.model.pagination

data class Page<out T>(
        val list: List<T>,
        val allItemsLoaded: Boolean
)