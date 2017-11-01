package com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.vh

import android.view.ViewGroup
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.page_list_item.NextPageError
import kotlinx.android.synthetic.main.item_next_page_error.view.*

class NextPageErrorViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<NextPageError>(parent, R.layout.item_next_page_error) {
    init {
        itemView.retry_Button.setOnClickListener {
            adapter?.pageIndicatorErrorRetryClicks?.onNext(Any())
        }
    }
}