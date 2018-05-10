package com.qwert2603.spenddemo.records_list

import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.util.Const
import com.qwert2603.spenddemo.records_list.entity.*
import com.qwert2603.spenddemo.records_list.vhs.DateSumViewHolder
import com.qwert2603.spenddemo.records_list.vhs.ProfitViewHolder
import com.qwert2603.spenddemo.records_list.vhs.RecordViewHolder
import com.qwert2603.spenddemo.records_list.vhs.TotalsViewHolder

class RecordsAdapter : BaseRecyclerViewAdapter<RecordsListItem>() {
    companion object {
        const val VIEW_TYPE_RECORD = 1
        const val VIEW_TYPE_DATE_SUM = 2
        const val VIEW_TYPE_PROFIT = 3
        const val VIEW_TYPE_TOTALS = 4
    }

    var showChangeKinds = true
    var showIds = true
    var showDatesInRecords = true

    override fun getItemViewTypeModel(m: RecordsListItem) = when (m) {
        is RecordUI -> VIEW_TYPE_RECORD
        is DateSumUI -> VIEW_TYPE_DATE_SUM
        is ProfitUI -> VIEW_TYPE_PROFIT
        is TotalsUi -> VIEW_TYPE_TOTALS
        else -> null!!
    }

    // because both spend's and profit's ids start from 1L.
    override fun getItemIdModel(m: RecordsListItem) = when (m) {
        is RecordUI -> m.id + 10_000_000L
        is DateSumUI -> m.date.time / Const.MILLIS_PER_DAY + 30_000_000L
        is ProfitUI -> m.id + 20_000_000L
        is TotalsUi -> 1918L
        else -> null!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_RECORD -> RecordViewHolder(parent)
        VIEW_TYPE_DATE_SUM -> DateSumViewHolder(parent)
        VIEW_TYPE_PROFIT -> ProfitViewHolder(parent)
        VIEW_TYPE_TOTALS -> TotalsViewHolder(parent)
        else -> null!!
    } as BaseRecyclerViewHolder<RecordsListItem>

    override fun onBindViewHolder(holder: BaseRecyclerViewHolder<IdentifiableLong>, position: Int, payloads: MutableList<Any>) {
        if (RecordsListAnimator.PAYLOAD_HIGHLIGHT in payloads) return
        super.onBindViewHolder(holder, position, payloads)
    }
}