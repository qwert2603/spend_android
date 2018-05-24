package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.qwert2603.spenddemo.records_list.entity.*

class RecordsListAdapter : ListAdapter<RecordsListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_SPEND -> SpendViewHolder(parent)
        VIEW_TYPE_DATE_SUM -> DateSumViewHolder(parent)
        VIEW_TYPE_PROFIT -> ProfitViewHolder(parent)
        VIEW_TYPE_TOTALS -> TotalsViewHolder(parent)
        VIEW_TYPE_MONTH_SUM -> MonthSumViewHolder(parent)
        else -> null!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? SpendViewHolder)?.bind(getItem(position) as? SpendUI)
        (holder as? ProfitViewHolder)?.bind(getItem(position) as? ProfitUI)
        (holder as? TotalsViewHolder)?.bind(getItem(position) as? TotalsUI)
        (holder as? DateSumViewHolder)?.bind(getItem(position) as? DateSumUI)
        (holder as? MonthSumViewHolder)?.bind(getItem(position) as? MonthSumUI)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is SpendUI -> VIEW_TYPE_SPEND
        is DateSumUI -> VIEW_TYPE_DATE_SUM
        is ProfitUI -> VIEW_TYPE_PROFIT
        is TotalsUI -> VIEW_TYPE_TOTALS
        is MonthSumUI -> VIEW_TYPE_MONTH_SUM
        else -> null!!
    }

    companion object {

        const val VIEW_TYPE_SPEND = 1
        const val VIEW_TYPE_DATE_SUM = 2
        const val VIEW_TYPE_PROFIT = 3
        const val VIEW_TYPE_TOTALS = 4
        const val VIEW_TYPE_MONTH_SUM = 5

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecordsListItem>() {
            override fun areItemsTheSame(oldItem: RecordsListItem, newItem: RecordsListItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: RecordsListItem, newItem: RecordsListItem) = oldItem == newItem
        }
    }
}