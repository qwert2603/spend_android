package com.qwert2603.spenddemo.records_list

import android.view.ViewGroup
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.records_list.entity.RecordsListItem

class RecordsAdapter : BaseRecyclerViewAdapter<RecordsListItem>() {
    companion object {
        const val VIEW_TYPE_RECORD = 1
    }

    override fun getItemViewTypeModel(m: RecordsListItem) = when (m) {
        is RecordUI -> VIEW_TYPE_RECORD
        else -> null!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_RECORD -> RecordViewHolder(parent)
        else -> null!!
    } as BaseRecyclerViewHolder<RecordsListItem>

}