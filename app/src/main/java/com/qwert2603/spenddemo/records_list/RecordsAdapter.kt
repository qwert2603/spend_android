package com.qwert2603.spenddemo.records_list

import android.view.ViewGroup
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.spenddemo.records_list.entity.AddRecordItem
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.records_list.entity.RecordsListItem

class RecordsAdapter : BaseRecyclerViewAdapter<RecordsListItem>() {
    companion object {
        private const val VIEW_TYPE_ADD_RECORD = 1
        private const val VIEW_TYPE_RECORD = 2
    }

    override fun getItemViewTypeModel(m: RecordsListItem) = when (m) {
        AddRecordItem -> VIEW_TYPE_ADD_RECORD
        is RecordUI -> VIEW_TYPE_RECORD
        else -> null!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_ADD_RECORD -> AddRecordViewHolder(parent)
        VIEW_TYPE_RECORD -> RecordViewHolder(parent)
        else -> null!!
    } as BaseRecyclerViewHolder<RecordsListItem>
}