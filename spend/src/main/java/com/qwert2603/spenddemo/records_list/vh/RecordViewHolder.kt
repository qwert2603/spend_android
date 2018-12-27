package com.qwert2603.spenddemo.records_list.vh

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
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
import kotlinx.android.synthetic.main.item_record.*

class RecordViewHolder(parent: ViewGroup) : BaseViewHolder<Record>(parent, R.layout.item_record) {

    override fun bind(t: Record, adapter: RecordsListAdapter) = with(itemView) {
        super.bind(t, adapter)

        itemView.isSelected = t.uuid in adapter.selectedRecordsUuids

        drawChange(t.change)

        date_TextView.setVisible(adapter.showDatesInRecords)
        DateTimeTextViews.render(
                dateTextView = date_TextView,
                timeTextView = time_TextView,
                date = t.date,
                time = t.time,
                showTimeAtAll = adapter.showTimesInRecords
        )
        val spannableStringBuilder = SpannableStringBuilder("${t.recordCategory.name} ${t.kind}")
        spannableStringBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, t.recordCategory.name.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        kind_TextView.text = spannableStringBuilder
        kind_TextView.setTextColor(resources.color(when (t.recordCategory.recordTypeId) {
            Const.RECORD_TYPE_ID_SPEND -> R.color.spend
            Const.RECORD_TYPE_ID_PROFIT -> R.color.profit
            else -> null!!
        }))
        value_TextView.text = t.value.toPointedString()
    }

    fun drawChange(recordChange: RecordChange?) = with(itemView) {
        t = t?.copy(change = recordChange)

        val t = t ?: return
        val adapter = adapter ?: return

        local_ImageView.setVisible(adapter.showChangeKinds)
        local_ImageView.setImageResource(when {
            t.change != null -> R.drawable.ic_local
            else -> R.drawable.ic_synced
        })
        if (t.change != null) {
            local_ImageView.setColorFilter(resources.color(when (t.change.isDelete) {
                true -> R.color.local_change_delete
                false -> R.color.local_change_update
            }))
        } else {
            local_ImageView.clearColorFilter()
        }

        val canInteract = t.isChangeable() || (adapter.selectMode && !t.isDeleted())
        isClickable = canInteract
        isLongClickable = canInteract

        val strike = t.change?.isDelete == true
        listOf(date_TextView, time_TextView, kind_TextView, value_TextView)
                .forEach { it.setStrike(strike) }
    }
}