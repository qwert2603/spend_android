package com.qwert2603.spenddemo.records_list_mvvm

import android.view.ViewGroup
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list_mvvm.entity.MonthSumUI
import com.qwert2603.spenddemo.utils.toPointedString
import com.qwert2603.spenddemo.utils.zeroToEmpty
import kotlinx.android.synthetic.main.item_month_sum.view.*
import java.text.SimpleDateFormat
import java.util.*

class MonthSumViewHolder(parent: ViewGroup) : BaseViewHolder<MonthSumUI>(parent, R.layout.item_month_sum) {

    companion object {
        private val MONTH_FORMAT = SimpleDateFormat("yyyy MMMM", Locale.getDefault())
    }

    override fun bind(t: MonthSumUI, adapter: RecordsListAdapter) = with(itemView) {
        super.bind(t, adapter)
        month_TextView.text = MONTH_FORMAT.format(t.date).toLowerCase()
        profitsSum_TextView.setVisible(t.showProfits)
        profitsSum_TextView.text = t.profits.toPointedString().zeroToEmpty()
        spendsSum_TextView.setVisible(t.showSpends)
        spendsSum_TextView.text = t.spends.toPointedString().zeroToEmpty()
    }
}