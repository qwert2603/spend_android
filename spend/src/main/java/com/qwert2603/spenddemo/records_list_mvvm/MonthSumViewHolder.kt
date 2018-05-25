package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list.entity.MonthSumUI
import com.qwert2603.spenddemo.utils.toPointedString
import com.qwert2603.spenddemo.utils.zeroToEmpty
import kotlinx.android.synthetic.main.item_month_sum.view.*
import java.text.SimpleDateFormat
import java.util.*

class MonthSumViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_month_sum, parent, false)) {

    companion object {
        private val MONTH_FORMAT = SimpleDateFormat("yyyy MMMM", Locale.getDefault())
    }

    fun bind(m: MonthSumUI?) = with(itemView) {
        if (m == null) return@with
        month_TextView.text = MONTH_FORMAT.format(m.date).toLowerCase()
        profitsSum_TextView.setVisible(m.showProfits)
        profitsSum_TextView.text = m.profits.toPointedString().zeroToEmpty()
        spendsSum_TextView.setVisible(m.showSpends)
        spendsSum_TextView.text = m.spends.toPointedString().zeroToEmpty()
    }
}