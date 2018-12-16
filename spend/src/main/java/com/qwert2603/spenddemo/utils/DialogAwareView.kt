package com.qwert2603.spenddemo.utils

import android.content.Intent
import android.support.v4.app.DialogFragment

interface DialogAwareView {
    fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?)

    var dialogShower: DialogShower

    interface DialogShower {
        fun showDialog(dialogFragment: DialogFragment, requestCode: Int)
    }
}