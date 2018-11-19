package com.qwert2603.spenddemo.records_list.vh

import android.view.ViewGroup
import com.qwert2603.andrlib.util.color
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.records_list.RecordsListAdapter
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.DateTimeTextViews
import com.qwert2603.spenddemo.utils.setStrike
import com.qwert2603.spenddemo.utils.toPointedString
import kotlinx.android.synthetic.main.item_record.view.*

class RecordViewHolder(parent: ViewGroup) : BaseViewHolder<Record>(parent, R.layout.item_record) {

    override fun bind(t: Record, adapter: RecordsListAdapter) = with(itemView) {
        super.bind(t, adapter)

        drawChange(t.change)

        date_TextView.setVisible(adapter.showDatesInRecords)
        DateTimeTextViews.render(
                dateTextView = date_TextView,
                timeTextView = time_TextView,
                date = t.date,
                time = t.time,
                showTimeAtAll = adapter.showTimesInRecords
        )
        kind_TextView.text = t.kind
        kind_TextView.setTextColor(resources.color(when (t.recordTypeId) {
            Const.RECORD_TYPE_ID_SPEND -> R.color.spend
            Const.RECORD_TYPE_ID_PROFIT -> R.color.profit
            else -> null!!
        }))
        value_TextView.text = t.value.toPointedString()

        isClickable = t.change?.changeKindId != Const.CHANGE_KIND_DELETE
        isLongClickable = t.change?.changeKindId != Const.CHANGE_KIND_DELETE

        val strike = t.change?.changeKindId == Const.CHANGE_KIND_DELETE
        listOf(date_TextView, time_TextView, kind_TextView, value_TextView)
                .forEach { it.setStrike(strike) }
    }

    fun drawChange(recordChange: RecordChange?) = with(itemView) {
        t = t?.copy(change = recordChange)
        val adapter = adapter ?: return

        local_ImageView.setVisible(adapter.showChangeKinds)
        local_ImageView.setImageResource(when {
            recordChange != null -> R.drawable.ic_local
            else -> R.drawable.ic_done_24dp
        })
        if (recordChange != null) {
            local_ImageView.setColorFilter(resources.color(when (recordChange.changeKindId) {
                Const.CHANGE_KIND_UPSERT -> R.color.local_change_edit
                Const.CHANGE_KIND_DELETE -> R.color.local_change_delete
                else -> null!!
            }))
        }
    }
}