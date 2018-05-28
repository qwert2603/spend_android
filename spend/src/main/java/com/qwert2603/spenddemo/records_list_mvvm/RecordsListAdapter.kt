package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.records_list.entity.*
import com.qwert2603.spenddemo.utils.FastDiffUtils

class RecordsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var showChangeKinds = true
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    var showIds = true
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    var showDatesInRecords = true
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    var list: List<RecordsListItem> = emptyList()
        set(value) {
            val oldList = field
            field = value

            // todo: background thread coroutines.
            FastDiffUtils.fastCalculateDiff(
                    oldList = oldList,
                    newList = field,
                    id = { this.id() },
                    compareOrder = { r1, r2 ->
                        return@fastCalculateDiff r2.time().compareTo(r1.time())
                                .takeIf { it != 0 }
                                ?: r2.priority().compareTo(r1.priority())
                                        .takeIf { it != 0 }
                                ?: r2.id.compareTo(r1.id)
                    },
                    isEqual = { r1, r2 -> r1 == r2 }
            )
                    .also { LogUtils.d("RecordsListAdapter fastCalculateDiff $it") }
                    .dispatchToAdapter(this)
        }

    var itemClicks: ((RecordsListItem) -> Unit)? = null
    var itemLongClicks: ((RecordsListItem) -> Unit)? = null

    override fun getItemCount() = list.size

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
        (holder as? SpendViewHolder)?.bind(list[position] as SpendUI, this)
        (holder as? ProfitViewHolder)?.bind(list[position] as ProfitUI, this)
        (holder as? TotalsViewHolder)?.bind(list[position] as TotalsUI, this)
        (holder as? DateSumViewHolder)?.bind(list[position] as DateSumUI, this)
        (holder as? MonthSumViewHolder)?.bind(list[position] as MonthSumUI, this)
    }

    override fun getItemViewType(position: Int) = when (list[position]) {
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

        private fun RecordsListItem.id() = when (this) {
            is SpendUI -> this.id + 1_000_000_000L
            is DateSumUI -> this.id + 3_000_000_000L
            is ProfitUI -> this.id + 2_000_000_000L
            is TotalsUI -> 1918L
            is MonthSumUI -> this.id + 4_000_000_000L
            else -> null!!
        }

        private fun RecordsListItem.time() = when (this) {
            is SpendUI -> this.date.time
            is DateSumUI -> this.date.time
            is ProfitUI -> this.date.time
            is TotalsUI -> Long.MIN_VALUE
            is MonthSumUI -> this.date.time
            else -> null!!
        }

        private fun RecordsListItem.priority() = when (this) {
            is SpendUI -> 5
            is ProfitUI -> 4
            is DateSumUI -> 3
            is MonthSumUI -> 2
            is TotalsUI -> 1
            else -> null!!
        }

    }
}