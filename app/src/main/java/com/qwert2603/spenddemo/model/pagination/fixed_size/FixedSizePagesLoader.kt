package com.qwert2603.spenddemo.model.pagination.fixed_size

import com.qwert2603.spenddemo.model.pagination.Page
import io.reactivex.Single

class FixedSizePagesLoader<T>(
        private val pageSize: Int = 20
) {
    private lateinit var source: (FixedSizeKey) -> Single<List<T>>
    private var loadedPagesCount = 0
    private var allItemsLoaded = false

    fun firstPage(newSource: (FixedSizeKey) -> Single<List<T>>): Single<Page<T>> {
        return newSource(FixedSizeKey(pageSize, 0))
                .map { list ->
                    allItemsLoaded = list.size < pageSize
                    loadedPagesCount = 1
                    source = newSource
                    Page(list, allItemsLoaded)
                }
    }

    fun nextPage(): Single<Page<T>> {
        if (allItemsLoaded) return Single.just(Page(emptyList(), true))
        return source(FixedSizeKey(pageSize, loadedPagesCount * pageSize))
                .map { list ->
                    allItemsLoaded = list.size < pageSize
                    ++loadedPagesCount
                    Page(list, allItemsLoaded)
                }
    }
}