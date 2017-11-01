package com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.page_list_item

data class AllItemsLoaded(val originalItemsCount: Int) : PageListItem {
    override val id = 239436367872309623L + originalItemsCount
}