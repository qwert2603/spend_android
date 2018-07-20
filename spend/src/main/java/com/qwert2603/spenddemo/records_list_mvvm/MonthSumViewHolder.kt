package com.qwert2603.spenddemo.records_list_mvvm

import android.content.res.Resources
import android.view.ViewGroup
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list_mvvm.entity.MonthSumUI
import com.qwert2603.spenddemo.utils.month
import com.qwert2603.spenddemo.utils.toPointedString
import com.qwert2603.spenddemo.utils.year
import com.qwert2603.spenddemo.utils.zeroToEmpty
import kotlinx.android.synthetic.main.item_month_sum.view.*
import java.util.*

class MonthSumViewHolder(parent: ViewGroup) : BaseViewHolder<MonthSumUI>(parent, R.layout.item_month_sum) {

    companion object {
        private fun Date.toMonthString(resources: Resources) = Calendar.getInstance()
                .also { it.time = this }
                .let { "${it.year} ${resources.getStringArray(R.array.month_names)[it.month]}" }
    }

    override fun bind(t: MonthSumUI, adapter: RecordsListAdapter) = with(itemView) {
        super.bind(t, adapter)
        month_TextView.text = t.date.toMonthString(resources)
        profitsSum_TextView.setVisible(t.showProfits)
        profitsSum_TextView.text = t.profits.toPointedString().zeroToEmpty()
        spendsSum_TextView.setVisible(t.showSpends)
        spendsSum_TextView.text = t.spends.toPointedString().zeroToEmpty()
    }
}