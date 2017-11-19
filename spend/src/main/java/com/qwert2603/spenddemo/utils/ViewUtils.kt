package com.qwert2603.spenddemo.utils

import android.graphics.Paint
import android.support.annotation.LayoutRes
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewAnimator

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View =
        LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)

fun ViewAnimator.showIfNotYet(child: Int, animate: Boolean = true) {
    val inA = inAnimation
    val outA = outAnimation
    if (!animate) {
        inAnimation = null
        outAnimation = null
    }
    if (child != displayedChild) {
        displayedChild = child
    }
    if (!animate) {
        inAnimation = inA
        outAnimation = outA
    }
}

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

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