package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list.entity.TotalsUI
import com.qwert2603.spenddemo.utils.toPointedString
import kotlinx.android.synthetic.main.item_totals.view.*

class TotalsViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_totals, parent, false)) {
    fun bind(m: TotalsUI?) = with(itemView) {
        if (m == null) return@with
        profits_TextView.setVisible(m.showProfits)
        spends_TextView.setVisible(m.showSpends)
        profits_TextView.text = resources.getString(
                R.string.text_total_items_format,
                resources.getQuantityString(R.plurals.profits, m.profitsCount, m.profitsCount),
                m.profitsSum.toPointedString()
        )
        spends_TextView.text = resources.getString(
                R.string.text_total_items_format,
                resources.getQuantityString(R.plurals.spends, m.spendsCount, m.spendsCount),
                m.spendsSum.toPointedString()
        )
        total_TextView.text = resources.getString(R.string.text_total_balance_format, m.totalBalance.toPointedString())
    }
}