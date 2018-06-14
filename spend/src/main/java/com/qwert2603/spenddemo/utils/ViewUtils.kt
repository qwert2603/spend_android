package com.qwert2603.spenddemo.utils

import android.graphics.Paint
import android.graphics.Rect
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

fun EditText.selectEnd() {
    setSelection(text.length)
}

fun View.getGlobalVisibleRectRightNow() = Rect().also { getGlobalVisibleRect(it) }