package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.records_list_mvvm.entity.*
import com.qwert2603.spenddemo.utils.FastDiffUtils

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

    var pendingMovedSpendId: Long? = null
    var pendingMovedProfitId: Long? = null

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
                    isEqual = { r1, r2 -> r1 == r2 },
                    possiblyMovedItemIds = listOfNotNull(
                            pendingMovedSpendId?.plus(ADDENDUM_ID_SPEND),
                            pendingMovedProfitId?.plus(ADDENDUM_ID_PROFIT)
                    )
            )
                    .also { LogUtils.d("RecordsListAdapter fastCalculateDiff $it") }
                    .dispatchToAdapter(this)

            pendingMovedSpendId = null
        }

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

        private const val ADDENDUM_ID_SPEND = 0L
        private const val ADDENDUM_ID_PROFIT = 1_000_000_000L
        private const val ADDENDUM_ID_DATE_SUM = 2_000_000_000L
        private const val ADDENDUM_ID_MONTH_SUM = 3_000_000_000L
        private const val ADDENDUM_ID_TOTALS = 4_000_000_000L

        private fun RecordsListItem.id() = when (this) {
            is SpendUI -> this.id + ADDENDUM_ID_SPEND
            is ProfitUI -> this.id + ADDENDUM_ID_PROFIT
            is DateSumUI -> this.id + ADDENDUM_ID_DATE_SUM
            is MonthSumUI -> this.id + ADDENDUM_ID_MONTH_SUM
            is TotalsUI -> this.id + ADDENDUM_ID_TOTALS
            else -> null!!
        }

        private fun RecordsListItem.time() = when (this) {
            is SpendUI -> this.date.time
            is ProfitUI -> this.date.time
            is DateSumUI -> this.date.time
            is MonthSumUI -> this.date.time
            is TotalsUI -> Long.MIN_VALUE
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