package com.qwert2603.spenddemo.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.TextView
import com.qwert2603.andrlib.util.color
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.model.entity.toFormattedString
import com.qwert2603.spenddemo.navigation.KeyboardManager

fun TextView.setStrike(strike: Boolean) {
    paintFlags = if (strike) {
        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}

fun EditText.setTextIfNotYet(text: String) {
    if (this.text.toString() != text) {
        this.setText(text)
    }
}

fun View.getGlobalVisibleRectRightNow() = Rect().also { getGlobalVisibleRect(it) }

object DateTimeTextViews {

    fun render(
            dateTextView: TextView,
            timeTextView: TextView,
            date: SDate?,
            time: STime?,
            showTimeAtAll: Boolean = true,
            timePanel: View = timeTextView
    ) {
        val resources = dateTextView.resources
        if (date == null) {
            dateTextView.setText(R.string.now_text)
            dateTextView.setTextColor(resources.color(R.color.date_default))
            timePanel.setVisible(false)
        } else {
            dateTextView.text = date.toFormattedString(resources)
            dateTextView.setTextColor(resources.color(android.R.color.black))
            timePanel.setVisible(showTimeAtAll)
            if (time == null) {
                timeTextView.text = resources.getString(R.string.no_time_text)
                timeTextView.setTextColor(resources.color(R.color.date_default))
            } else {
                timeTextView.text = time.toString()
                timeTextView.setTextColor(resources.color(android.R.color.black))
            }
        }
    }
}

fun View.onPreDraw(action: () -> Boolean) {
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            viewTreeObserver.removeOnPreDrawListener(this)
            return action()
        }
    })
}

fun AlertDialog.setShowing(showing: Boolean, keyboardContext: Context) {
    if (showing) {
        (keyboardContext as KeyboardManager).hideKeyboard()
        show()
    } else {
        dismiss()
    }
}

fun View.getBackgroundColor() = (background as? ColorDrawable)?.color

fun View.animateBackgroundColor(from: Int, to: Int, duration: Long? = null) {
    ObjectAnimator.ofArgb(this, "backgroundColor", from, to)
            .also { animator -> duration?.let { animator.duration = it } }
            .start()
}