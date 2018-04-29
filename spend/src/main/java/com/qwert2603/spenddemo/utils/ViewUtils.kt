package com.qwert2603.spenddemo.utils

import android.graphics.Paint
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView

fun TextView.setStrike(strike: Boolean) {
    paintFlags = if (strike) {
        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}

fun View.setSelectableItemBackground() {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
    setBackgroundResource(typedValue.resourceId)
}

fun EditText.selectEnd() {
    setSelection(text.length)
}