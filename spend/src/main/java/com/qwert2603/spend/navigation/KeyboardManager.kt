package com.qwert2603.spend.navigation

import android.widget.EditText

interface KeyboardManager {
    fun hideKeyboard(removeFocus: Boolean = true)
    fun showKeyboard(editText: EditText)
}