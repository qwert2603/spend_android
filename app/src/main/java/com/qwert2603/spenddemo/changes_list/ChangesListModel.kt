package com.qwert2603.spenddemo.changes_list

import com.qwert2603.spenddemo.base_mvi.load_refresh.InitialModelHolder
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.ListModelHolder
import com.qwert2603.spenddemo.model.entity.Change
import com.qwert2603.spenddemo.model.pagination.Page

data class ChangesListModel(
        override val nextPageLoading: Boolean,
        override val nextPageError: Throwable?,
        override val allItemsLoaded: Boolean,
        override val showingList: List<Change>
) : InitialModelHolder<Page<Change>>, ListModelHolder<Change> {
    override fun changeInitialModel(i: Page<Change>) = copy(allItemsLoaded = i.allItemsLoaded, showingList = i.list)

    override fun changeListModel(nextPageLoading: Boolean, nextPageError: Throwable?, allItemsLoaded: Boolean, nextPageList: List<Change>?) = copy(
            nextPageLoading = nextPageLoading,
            nextPageError = nextPageError,
            allItemsLoaded = allItemsLoaded,
            showingList = showingList + (nextPageList ?: emptyList())
    )
}