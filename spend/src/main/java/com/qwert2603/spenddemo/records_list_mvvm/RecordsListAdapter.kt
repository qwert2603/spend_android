package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.qwert2603.spenddemo.records_list.entity.ProfitUI
import com.qwert2603.spenddemo.records_list.entity.RecordsListItem
import com.qwert2603.spenddemo.records_list.entity.SpendUI

class RecordsListAdapter : ListAdapter<RecordsListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        1 -> SpendViewHolder(parent)
        2 -> ProfitViewHolder(parent)
        else -> null!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? SpendViewHolder)?.bind(getItem(position) as? SpendUI)
        (holder as? ProfitViewHolder)?.bind(getItem(position) as? ProfitUI)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is SpendUI -> 1
        is ProfitUI -> 2
        else -> null!!
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecordsListItem>() {
            override fun areItemsTheSame(oldItem: RecordsListItem, newItem: RecordsListItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: RecordsListItem, newItem: RecordsListItem) = oldItem == newItem
        }
    }
}