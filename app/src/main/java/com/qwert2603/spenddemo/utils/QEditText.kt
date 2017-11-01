package com.qwert2603.spenddemo.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

// todo: correct naming.
class QEditText(private val editText: EditText) {
    private var userInput = true

    init {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                LogUtils.d("${this@QEditText.hashCode()} afterTextChanged $s")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                LogUtils.d("${this@QEditText.hashCode()} beforeTextChanged $s")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                LogUtils.d("${this@QEditText.hashCode()} onTextChanged $s")
            }
        })
    }

    fun setText(text: String) {
        LogUtils.d("${this@QEditText.hashCode()} userInput = false")
        userInput = false
        if (editText.text.toString() != text) {
            editText.setText(text)
            editText.setSelection(text.length)
        }
        LogUtils.d("${this@QEditText.hashCode()} userInput = true")
        userInput = true
    }

    fun userInputs(): Observable<String> = RxTextView.textChanges(editText)
            .skipInitialValue()
            .filter { userInput }
            .map { it.toString() }
}