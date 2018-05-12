package com.qwert2603.spenddemo.records_list

import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.util.Const
import com.qwert2603.spenddemo.records_list.entity.*
import com.qwert2603.spenddemo.records_list.vhs.*

class RecordsAdapter : BaseRecyclerViewAdapter<RecordsListItem>() {
    companion object {
        const val VIEW_TYPE_SPEND = 1
        const val VIEW_TYPE_DATE_SUM = 2
        const val VIEW_TYPE_PROFIT = 3
        const val VIEW_TYPE_TOTALS = 4
        const val VIEW_TYPE_MONTH_SUM = 5
    }

    var showChangeKinds = true
    var showIds = true
    var showDatesInRecords = true

    override fun getItemViewTypeModel(m: RecordsListItem) = when (m) {
        is SpendUI -> VIEW_TYPE_SPEND
        is DateSumUI -> VIEW_TYPE_DATE_SUM
        is ProfitUI -> VIEW_TYPE_PROFIT
        is TotalsUI -> VIEW_TYPE_TOTALS
        is MonthSumUI -> VIEW_TYPE_MONTH_SUM
        else -> null!!
    }

    // because both spend's and profit's ids start from 1L.
    override fun getItemIdModel(m: RecordsListItem) = when (m) {
        is SpendUI -> m.id + 1_000_000_000L
        is DateSumUI -> m.date.time / Const.MILLIS_PER_DAY + 3_000_000_000L
        is ProfitUI -> m.id + 2_000_000_000L
        is TotalsUI -> 1918L
        is MonthSumUI -> m.month.time / Const.MILLIS_PER_DAY + 4_000_000_000L
        else -> null!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_SPEND -> SpendViewHolder(parent)
        VIEW_TYPE_DATE_SUM -> DateSumViewHolder(parent)
        VIEW_TYPE_PROFIT -> ProfitViewHolder(parent)
        VIEW_TYPE_TOTALS -> TotalsViewHolder(parent)
        VIEW_TYPE_MONTH_SUM -> MonthSumViewHolder(parent)
        else -> null!!
    } as BaseRecyclerViewHolder<RecordsListItem>

    override fun onBindViewHolder(holder: BaseRecyclerViewHolder<IdentifiableLong>, position: Int, payloads: MutableList<Any>) {
        if (RecordsListAnimator.PAYLOAD_HIGHLIGHT in payloads) return
        super.onBindViewHolder(holder, position, payloads)
    }
}