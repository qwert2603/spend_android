package com.qwert2603.spend.utils

import android.content.Intent

interface DialogAwareView {
    fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?)

    var dialogShower: DialogShower

    interface DialogShower {
        val fragmentWho: String
    }
}