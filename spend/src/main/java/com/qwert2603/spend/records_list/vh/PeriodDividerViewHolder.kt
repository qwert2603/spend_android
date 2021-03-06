package com.qwert2603.spend.records_list.vh

import android.view.ViewGroup
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.PeriodDivider
import com.qwert2603.spend.records_list.RecordsListAdapter
import com.qwert2603.spend.utils.formatTime
import kotlinx.android.synthetic.main.item_period_divider.*

class PeriodDividerViewHolder(parent: ViewGroup) : BaseViewHolder<PeriodDivider>(parent, R.layout.item_period_divider) {

    override fun bind(t: PeriodDivider, adapter: RecordsListAdapter) = with(itemView) {
        super.bind(t, adapter)
        period_TextView.text = resources.formatTime(t.interval)
    }
}