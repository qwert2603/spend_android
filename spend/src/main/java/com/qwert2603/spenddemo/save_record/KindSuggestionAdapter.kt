package com.qwert2603.spenddemo.save_record

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
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.model.entity.RecordKind
import kotlinx.android.synthetic.main.item_suggestion.view.*

class KindSuggestionAdapter(context: Context, suggestions: List<RecordKind>, s: String? = null)
    : ArrayAdapter<RecordKind>(context, 0, suggestions) {

    private val search = s?.toLowerCase()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: parent.inflate(R.layout.item_suggestion)
        val recordKind = getItem(position)!!
        val s = "${recordKind.recordCategory.name} / ${recordKind.kind}"
        val spannableStringBuilder = SpannableStringBuilder(s)
        if (search != null) {
            val indexOf = s.toLowerCase().indexOf(search, startIndex = recordKind.recordCategory.name.length + 3)
            if (indexOf >= 0) {
                spannableStringBuilder.setSpan(StyleSpan(Typeface.BOLD), indexOf, indexOf + search.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        view.suggestion_TextView.text = spannableStringBuilder
        return view
    }
}