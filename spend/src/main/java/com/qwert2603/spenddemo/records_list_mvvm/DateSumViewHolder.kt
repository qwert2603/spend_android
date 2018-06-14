package com.qwert2603.spenddemo.records_list_mvvm

import android.view.ViewGroup
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list_mvvm.entity.DateSumUI
import com.qwert2603.spenddemo.utils.toFormattedString
import com.qwert2603.spenddemo.utils.toPointedString
import com.qwert2603.spenddemo.utils.zeroToEmpty
import kotlinx.android.synthetic.main.item_date_sum.view.*

class DateSumViewHolder(parent: ViewGroup) : BaseViewHolder<DateSumUI>(parent, R.layout.item_date_sum) {

    override fun bind(t: DateSumUI, adapter: RecordsListAdapter) = with(itemView) {
        super.bind(t, adapter)
        date_TextView.text = t.date.toFormattedString(resources)
        profitsSum_TextView.setVisible(t.showProfits)
        profitsSum_TextView.text = t.profits.toPointedString().zeroToEmpty()
        spendsSum_TextView.setVisible(t.showSpends)
        spendsSum_TextView.text = t.spends.toPointedString().zeroToEmpty()
    }
}