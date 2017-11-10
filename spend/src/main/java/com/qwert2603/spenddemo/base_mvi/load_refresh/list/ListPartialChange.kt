package com.qwert2603.spenddemo.base_mvi.load_refresh.list

import com.qwert2603.spenddemo.base_mvi.PartialChange
import com.qwert2603.spenddemo.model.pagination.Page

/**
 * Partial changes those for view state that extends [ListModelHolder].
 */
sealed class ListPartialChange : PartialChange {
    data class NextPageLoading(private val ignored: Unit = Unit) : ListPartialChange()
    data class NextPageError(val t: Throwable) : ListPartialChange()
    data class NextPageCancelled(private val ignored: Unit = Unit) : ListPartialChange()
    data class NextPageLoaded<out T>(val page: Page<T>) : ListPartialChange()
}