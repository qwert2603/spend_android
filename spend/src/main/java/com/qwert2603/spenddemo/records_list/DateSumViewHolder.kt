package com.qwert2603.spenddemo.records_list

import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list.entity.DateSum
import com.qwert2603.spenddemo.utils.toFormattedString
import kotlinx.android.synthetic.main.item_date_sum.view.*

class DateSumViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<DateSum>(parent, R.layout.item_date_sum) {
    override fun bind(m: DateSum) = with(itemView) {
        super.bind(m)
        date_TextView.text = m.date.toFormattedString(resources)
        sum_TextView.text = m.sum.toString()
    }
}