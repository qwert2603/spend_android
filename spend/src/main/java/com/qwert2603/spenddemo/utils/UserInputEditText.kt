package com.qwert2603.spenddemo.utils

import android.widget.EditText
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qwert2603.andrlib.util.LogUtils
import io.reactivex.Observable

class UserInputEditText(private val editText: EditText) {
    private var userInput = true

    init {
        editText.isSaveEnabled = false
    }

    fun setText(text: String) {
        LogUtils.d("${this@UserInputEditText.hashCode()} userInput = false")
        userInput = false
        if (editText.text.toString() != text) {
            editText.setText(text)
            editText.setSelection(text.length)
        }
        LogUtils.d("${this@UserInputEditText.hashCode()} userInput = true")
        userInput = true
    }

    fun userInputs(): Observable<String> = RxTextView.textChanges(editText)
            .skipInitialValue()
            .filter {
                LogUtils.d("${this@UserInputEditText.hashCode()} filter $userInput $it")
                userInput
            }
            .map { it.toString() }
}