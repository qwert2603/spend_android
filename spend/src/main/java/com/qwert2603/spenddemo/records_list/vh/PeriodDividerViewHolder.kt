package com.qwert2603.spenddemo.records_list.vh

import android.view.ViewGroup
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.model.entity.Days
import com.qwert2603.spenddemo.model.entity.Minutes
import com.qwert2603.spenddemo.model.entity.PeriodDivider
import com.qwert2603.spenddemo.records_list.RecordsListAdapter
import com.qwert2603.spenddemo.utils.formatTime
import kotlinx.android.synthetic.main.item_period_divider.*

class PeriodDividerViewHolder(parent: ViewGroup) : BaseViewHolder<PeriodDivider>(parent, R.layout.item_period_divider) {

    override fun bind(t: PeriodDivider, adapter: RecordsListAdapter) = with(itemView) {
        super.bind(t, adapter)
        period_TextView.text = when (t.interval) {
            is Days -> resources.getQuantityString(R.plurals.days, t.interval.days, t.interval.days)
            is Minutes -> resources.formatTime(t.interval.minutes)
        }
    }
}