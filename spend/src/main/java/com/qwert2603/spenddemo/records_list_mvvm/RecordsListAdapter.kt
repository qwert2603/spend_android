package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.qwert2603.spenddemo.records_list_mvvm.entity.*

// todo: delegate adapters.
class RecordsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var showChangeKinds = true
        set(value) {
            if (value == field) return
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    var showIds = true
        set(value) {
            if (value == field) return
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    var showDatesInRecords = true
        set(value) {
            if (value == field) return
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    var showTimesInRecords = true
        set(value) {
            if (value == field) return
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    var list: List<RecordsListItem> = emptyList()

    var itemClicks: ((RecordsListItem) -> Unit)? = null
    var itemLongClicks: ((RecordsListItem) -> Unit)? = null

    override fun getItemCount() = list.size

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_SPEND -> SpendViewHolder(parent)
        VIEW_TYPE_PROFIT -> ProfitViewHolder(parent)
        VIEW_TYPE_DATE_SUM -> DateSumViewHolder(parent)
        VIEW_TYPE_MONTH_SUM -> MonthSumViewHolder(parent)
        VIEW_TYPE_TOTALS -> TotalsViewHolder(parent)
        else -> null!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? SpendViewHolder)?.bind(list[position] as SpendUI, this)
        (holder as? ProfitViewHolder)?.bind(list[position] as ProfitUI, this)
        (holder as? DateSumViewHolder)?.bind(list[position] as DateSumUI, this)
        (holder as? MonthSumViewHolder)?.bind(list[position] as MonthSumUI, this)
        (holder as? TotalsViewHolder)?.bind(list[position] as TotalsUI, this)
    }

    override fun getItemViewType(position: Int) = when (list[position]) {
        is SpendUI -> VIEW_TYPE_SPEND
        is ProfitUI -> VIEW_TYPE_PROFIT
        is DateSumUI -> VIEW_TYPE_DATE_SUM
        is MonthSumUI -> VIEW_TYPE_MONTH_SUM
        is TotalsUI -> VIEW_TYPE_TOTALS
        else -> null!!
    }

    companion object {
        const val VIEW_TYPE_SPEND = 1
        const val VIEW_TYPE_PROFIT = 2
        const val VIEW_TYPE_DATE_SUM = 3
        const val VIEW_TYPE_MONTH_SUM = 4
        const val VIEW_TYPE_TOTALS = 5
    }
}