package com.qwert2603.spenddemo.records_list

import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.andrlib.util.color
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list.entity.ProfitUI
import com.qwert2603.spenddemo.utils.toFormattedString
import kotlinx.android.synthetic.main.item_record.view.*

class ProfitViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<ProfitUI>(parent, R.layout.item_profit) {
    override fun bind(m: ProfitUI) = with(itemView) {
        super.bind(m)

        val showIds = (adapter as? RecordsAdapter)?.showIds ?: true
        val showChangeKinds = (adapter as? RecordsAdapter)?.showChangeKinds ?: true
        val showDatesInRecords = (adapter as? RecordsAdapter)?.showDatesInRecords ?: true

        local_ImageView.setVisible(showChangeKinds)
        id_TextView.setVisible(showIds)
        date_FrameLayout.setVisible(showDatesInRecords)

        local_ImageView.setImageResource(R.drawable.ic_done_24dp)
        local_ImageView.setColorFilter(resources.color(R.color.anth))

        id_TextView.text = m.id.toString()
        date_TextView.text = m.date.toFormattedString(resources)
        kind_TextView.text = m.kind
        value_TextView.text = m.value.toString()
    }
}