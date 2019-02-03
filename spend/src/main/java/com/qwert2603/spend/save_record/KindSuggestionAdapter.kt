package com.qwert2603.spend.save_record

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.RecordKindAggregation
import kotlinx.android.synthetic.main.item_suggestion.view.*

class KindSuggestionAdapter(context: Context, suggestions: List<RecordKindAggregation>, s: String? = null, private val withCategory: Boolean)
    : ArrayAdapter<RecordKindAggregation>(context, 0, suggestions) {

    private val search = s?.toLowerCase()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: parent.inflate(R.layout.item_suggestion)
        val recordKind = getItem(position)!!
        val s = if (withCategory) {
            "${recordKind.recordCategory.name} / ${recordKind.kind}"
        } else {
            recordKind.kind
        }
        val spannableStringBuilder = SpannableStringBuilder(s)
        if (search != null) {
            val indexOf = s.toLowerCase().indexOf(
                    string = search,
                    startIndex = if (withCategory) recordKind.recordCategory.name.length + 3 else 0
            )
            if (indexOf >= 0) {
                spannableStringBuilder.setSpan(StyleSpan(Typeface.BOLD), indexOf, indexOf + search.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        view.suggestion_TextView.text = spannableStringBuilder
        return view
    }
}