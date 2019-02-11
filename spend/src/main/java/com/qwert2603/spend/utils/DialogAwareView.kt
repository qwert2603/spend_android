package com.qwert2603.spend.utils

import android.content.Intent
import androidx.fragment.app.DialogFragment

interface DialogAwareView {
    fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?)

    var dialogShower: DialogShower

    interface DialogShower {
        fun showDialog(dialogFragment: DialogFragment, requestCode: Int)
    }
}