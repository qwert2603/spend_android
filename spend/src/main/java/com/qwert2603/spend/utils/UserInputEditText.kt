package com.qwert2603.spend.utils

import android.widget.AutoCompleteTextView
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.jakewharton.rxbinding3.widget.textChanges
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
        if (editText.text.toString() != text) {
            val prevSelectionEndOffset = editText.text.length - editText.selectionEnd
            editText.setText(text)
            editText.setSelection((text.length - prevSelectionEndOffset).coerceIn(0..text.length))
        }
        userInput = true
    }

    fun userInputs(): Observable<String> = editText
            .textChanges()
            .skipInitialValue()
            .filter { userInput }
            .map { it.toString() }
}