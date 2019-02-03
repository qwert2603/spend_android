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
import com.qwert2603.spend.model.entity.RecordCategoryAggregation
import kotlinx.android.synthetic.main.item_suggestion.view.*

class CategorySuggestionAdapter(context: Context, suggestions: List<RecordCategoryAggregation>, s: String? = null)
    : ArrayAdapter<RecordCategoryAggregation>(context, 0, suggestions) {

    private val search = s?.toLowerCase()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: parent.inflate(R.layout.item_suggestion)
        val category = getItem(position)!!
        val s = category.recordCategory.name
        val spannableStringBuilder = SpannableStringBuilder(s)
        if (search != null) {
            val indexOf = s.toLowerCase().indexOf(search)
            if (indexOf >= 0) {
                spannableStringBuilder.setSpan(StyleSpan(Typeface.BOLD), indexOf, indexOf + search.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        view.suggestion_TextView.text = spannableStringBuilder
        return view
    }
}