package com.qwert2603.spend.records_list.vh

import android.view.ViewGroup
import com.qwert2603.andrlib.util.color
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.DaySum
import com.qwert2603.spend.model.entity.toFormattedString
import com.qwert2603.spend.records_list.RecordsListAdapter
import com.qwert2603.spend.utils.toPointedString
import com.qwert2603.spend.utils.zeroToEmpty
import kotlinx.android.synthetic.main.item_date_sum.*

class DaySumViewHolder(parent: ViewGroup, clickable: Boolean) : BaseViewHolder<DaySum>(parent, R.layout.item_date_sum) {

    init {
        itemView.isClickable = clickable
        itemView.isLongClickable = clickable
    }

    override fun bind(t: DaySum, adapter: RecordsListAdapter) = with(itemView) {
        super.bind(t, adapter)
        date_TextView.text = t.day.toFormattedString(resources)

        balance_TextView.setVisible(adapter.showBalancesInSums)
        profitsSum_TextView.setVisible(!adapter.showBalancesInSums)
        spendsSum_TextView.setVisible(!adapter.showBalancesInSums)

        if (adapter.showBalancesInSums) {
            balance_TextView.text = t.balance.toPointedString().zeroToEmpty()
            balance_TextView.setTextColor(resources.color(
                    if (t.balance >= 0) {
                        R.color.balance_positive
                    } else {
                        R.color.balance_negative
                    }
            ))
        } else {
            profitsSum_TextView.setVisible(t.showProfits)
            profitsSum_TextView.text = t.profits.toPointedString().zeroToEmpty()
            spendsSum_TextView.setVisible(t.showSpends)
            spendsSum_TextView.text = t.spends.toPointedString().zeroToEmpty()
        }
    }
}