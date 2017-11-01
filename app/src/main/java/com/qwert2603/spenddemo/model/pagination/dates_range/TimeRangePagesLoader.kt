package com.qwert2603.spenddemo.model.pagination.dates_range

import com.qwert2603.spenddemo.utils.Const
import io.reactivex.Single

class TimeRangePagesLoader<T>(
        private val millisPerPage: Long = Const.DAYS_PER_WEEK * Const.MILLIS_PER_DAY,
        private val start: Long = System.currentTimeMillis(),
        private val earliest: Long = start - 366 * Const.MILLIS_PER_DAY
) {
    private lateinit var source: (TimeRangeKey) -> Single<List<T>>
    private var loadedPagesCount = 0
    private var allItemsLoaded = false

    fun firstPage(newSource: (TimeRangeKey) -> Single<List<T>>): Single<TimeRangePage<T>> {
        val datesRange = createDatesRangeKey(0)
        return newSource(datesRange)
                .map {
                    allItemsLoaded = datesRange.fromMillis <= earliest
                    loadedPagesCount = 1
                    source = newSource
                    TimeRangePage(datesRange, it, allItemsLoaded)
                }
    }

    fun nextPage(): Single<TimeRangePage<T>> {
        val datesRange = createDatesRangeKey(loadedPagesCount)
        if (allItemsLoaded) return Single.just(TimeRangePage(datesRange, emptyList(), true))
        return source(datesRange)
                .map {
                    allItemsLoaded = datesRange.fromMillis <= earliest
                    ++loadedPagesCount
                    TimeRangePage(datesRange, it, allItemsLoaded)
                }
    }

    private fun createDatesRangeKey(pageNumber: Int) = TimeRangeKey(
            fromMillis = start - (pageNumber + 1) * millisPerPage,
            toMillis = start - (pageNumber) * millisPerPage
    )
}