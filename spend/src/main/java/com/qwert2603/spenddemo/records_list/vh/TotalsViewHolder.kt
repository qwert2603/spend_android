package com.qwert2603.spenddemo.records_list.vh

import android.view.ViewGroup
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.model.entity.Totals
import com.qwert2603.spenddemo.records_list.RecordsListAdapter
import com.qwert2603.spenddemo.utils.toPointedString
import kotlinx.android.synthetic.main.item_totals.*

class TotalsViewHolder(parent: ViewGroup) : BaseViewHolder<Totals>(parent, R.layout.item_totals) {

    override fun bind(t: Totals, adapter: RecordsListAdapter) = with(itemView) {
        super.bind(t, adapter)
        profits_TextView.setVisible(t.showProfits)
        spends_TextView.setVisible(t.showSpends)
        profits_TextView.text = resources.getString(
                R.string.text_total_items_format,
                resources.getQuantityString(R.plurals.profits, t.profitsCount, t.profitsCount),
                t.profitsSum.toPointedString()
        )
        spends_TextView.text = resources.getString(
                R.string.text_total_items_format,
                resources.getQuantityString(R.plurals.spends, t.spendsCount, t.spendsCount),
                t.spendsSum.toPointedString()
        )
        total_TextView.text = resources.getString(R.string.text_total_sum_format, t.totalBalance.toPointedString())
    }
}