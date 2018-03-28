package com.qwert2603.spenddemo.utils

import android.widget.EditText
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

class UserInputEditText(private val editText: EditText) {
    private var userInput = true

    init {
        editText.isSaveEnabled = false
    }

    fun setText(text: String) {
        userInput = false
        if (editText.text.toString() != text) {
            editText.setText(text)
            editText.setSelection(text.length)
        }
        userInput = true
    }

    fun userInputs(): Observable<String> = RxTextView.textChanges(editText)
            .skipInitialValue()
            .filter { userInput }
            .map { it.toString() }
}