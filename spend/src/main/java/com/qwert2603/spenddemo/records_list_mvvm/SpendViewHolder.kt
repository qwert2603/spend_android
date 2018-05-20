package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list.entity.SpendUI
import com.qwert2603.spenddemo.utils.toFormattedString
import com.qwert2603.spenddemo.utils.toPointedString
import com.qwert2603.spenddemo.utils.zeroToEmpty
import kotlinx.android.synthetic.main.item_spend.view.*

class SpendViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_spend, parent, false)) {

    private var spendId: Long? = null

    init {
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                itemView.date_TextView,
                14,
                16,
                1,
                TypedValue.COMPLEX_UNIT_SP
        )

        itemView.setOnLongClickListener {
            spendId?.let {
                RecordsListViewModel.EXECUTOR.execute {
                    RecordsListViewModel.LOCAL_DB.spendsDao().removeSpend(it)
                }
            }
            true
        }
    }

    fun bind(m: SpendUI?) = with(itemView) {
        spendId = m?.id
        val showIds = false
        val showChangeKinds = false
        val showDatesInRecords = true

        local_ImageView.setVisible(showChangeKinds)
        id_TextView.setVisible(showIds)
        date_TextView.setVisible(showDatesInRecords)

        if (m != null) {
            id_TextView.text = m.id.toString()
            date_TextView.text = m.date.toFormattedString(resources)
            kind_TextView.text = m.kind
            value_TextView.text = m.value.toLong().toPointedString().zeroToEmpty()
        } else {
            id_TextView.text = "..."
            date_TextView.text = "..."
            kind_TextView.text = "..."
            value_TextView.text = "..."
        }
    }
}