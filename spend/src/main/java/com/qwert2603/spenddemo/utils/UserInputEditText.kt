package com.qwert2603.spenddemo.utils

import android.support.design.widget.TextInputEditText
import android.widget.AutoCompleteTextView
import android.widget.EditText
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

/**
this is not custom view, because there are a lot of different views for editing text:
[EditText], [AutoCompleteTextView], [TextInputEditText] and so on.
and [UserInputEditText] can work with them all.
 */
class UserInputEditText(private val editText: EditText) {
    private var userInput = true

    init {
        editText.isSaveEnabled = false
    }

    fun setText(text: String) {
        userInput = false
        editText.setTextIfNotYet(text)
        userInput = true
    }

    fun userInputs(): Observable<String> = RxTextView.textChanges(editText)
            .skipInitialValue()
            .filter { userInput }
            .map { it.toString() }
}