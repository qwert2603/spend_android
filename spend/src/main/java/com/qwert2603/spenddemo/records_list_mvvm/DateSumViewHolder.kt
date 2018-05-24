package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list.entity.DateSumUI
import com.qwert2603.spenddemo.utils.toFormattedString
import com.qwert2603.spenddemo.utils.toPointedString
import com.qwert2603.spenddemo.utils.zeroToEmpty
import kotlinx.android.synthetic.main.item_date_sum.view.*

class DateSumViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_date_sum, parent, false)) {
    fun bind(m: DateSumUI?) = with(itemView) {
        if (m == null) return@with
        date_TextView.text = m.date.toFormattedString(resources)
        profitsSum_TextView.setVisible(m.showProfits)
        profitsSum_TextView.text = m.profits.toPointedString().zeroToEmpty()
        spendsSum_TextView.setVisible(m.showSpends)
        spendsSum_TextView.text = m.spends.toPointedString().zeroToEmpty()
    }
}