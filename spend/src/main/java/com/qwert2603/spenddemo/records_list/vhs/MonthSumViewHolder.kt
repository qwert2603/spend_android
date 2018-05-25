package com.qwert2603.spenddemo.records_list.vhs

import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list.entity.MonthSumUI
import com.qwert2603.spenddemo.utils.toPointedString
import com.qwert2603.spenddemo.utils.zeroToEmpty
import kotlinx.android.synthetic.main.item_month_sum.view.*
import java.text.SimpleDateFormat
import java.util.*

class MonthSumViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<MonthSumUI>(parent, R.layout.item_month_sum) {

    companion object {
        private val MONTH_FORMAT = SimpleDateFormat("yyyy MMMM", Locale.getDefault())
    }

    override fun bind(m: MonthSumUI) = with(itemView) {
        super.bind(m)
        month_TextView.text = MONTH_FORMAT.format(m.date).toLowerCase()
        profitsSum_TextView.setVisible(m.showProfits)
        profitsSum_TextView.text = m.profits.toPointedString().zeroToEmpty()
        spendsSum_TextView.setVisible(m.showSpends)
        spendsSum_TextView.text = m.spends.toPointedString().zeroToEmpty()
    }
}