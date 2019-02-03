package com.qwert2603.spend.records_list_view

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.andrlib.util.color
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.Record
import com.qwert2603.spend.model.entity.toFormattedString
import com.qwert2603.spend.utils.Const
import com.qwert2603.spend.utils.toPointedString
import kotlinx.android.synthetic.main.item_record_list_view.view.*

class RecordViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<Record>(parent, R.layout.item_record_list_view) {
    override fun bind(m: Record) = with(itemView) {
        super.bind(m)

        val date = m.date.toFormattedString(resources)
        val time = m.time?.toString() ?: resources.getString(R.string.no_time_text)
        val category = m.recordCategory.name
        val kind = m.kind
        val value = m.value.toPointedString()

        val divider = " / "

        val text = listOf(date, time, category, kind, value)
                .reduce { acc, s -> "$acc$divider$s" }

        val spannableStringBuilder = SpannableStringBuilder(text)

        val categoryStart = date.length + divider.length + time.length + divider.length
        val categoryEnd = categoryStart + category.length
        val kindStart = categoryEnd + divider.length
        val kindEnd = kindStart + kind.length

        val color = resources.color(when (m.recordCategory.recordTypeId) {
            Const.RECORD_TYPE_ID_SPEND -> R.color.spend
            Const.RECORD_TYPE_ID_PROFIT -> R.color.profit
            else -> null!!
        })

        spannableStringBuilder.setSpan(StyleSpan(Typeface.BOLD), categoryStart, categoryEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStringBuilder.setSpan(ForegroundColorSpan(color), categoryStart, categoryEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStringBuilder.setSpan(ForegroundColorSpan(color), kindStart, kindEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        record_TextView.text = spannableStringBuilder
    }
}