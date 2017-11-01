package com.qwert2603.spenddemo.navigation

import android.widget.EditText

interface KeyboardManager {
    fun hideKeyboard(removeFocus: Boolean = true)
    fun showKeyboard(editText: EditText)
}