package com.qwert2603.spenddemo.base_mvi.load_refresh.list

import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.page_list_item.AllItemsLoaded
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.page_list_item.NextPageError
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.page_list_item.NextPageLoading
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.vh.AllItemsLoadedViewHolder
import com.qwert2603.spenddemo.model.entity.IdentifiableLong

/**
 * Interface for model that can be used id [ListPresenter].
 */
interface ListModelHolder<T : IdentifiableLong> {
    val nextPageLoading: Boolean
    val nextPageError: Throwable?
    val allItemsLoaded: Boolean
    val showingList: List<T>

    fun pageIndicatorItem() = when {
        nextPageLoading -> NextPageLoading()
        nextPageError != null -> NextPageError()
        allItemsLoaded -> AllItemsLoaded(originalItemsCount())
        else -> null
    }

    /**
     * @return count of items that loaded by pages.
     * For example, if feed list consist of news items and date items
     * (news items are loaded by pages and date items are added locally after grouping news items),
     * then [originalItemsCount] == count of news items.
     * This "count" also will be shown in [AllItemsLoadedViewHolder]. See [AllItemsLoaded.originalItemsCount].
     */
    fun originalItemsCount() = showingList.size

    /**
     * @return new view state with changed list model.
     */
    fun changeListModel(
            nextPageLoading: Boolean,
            nextPageError: Throwable?,
            allItemsLoaded: Boolean,
            nextPageList: List<T>?
    ): ListModelHolder<T>
}