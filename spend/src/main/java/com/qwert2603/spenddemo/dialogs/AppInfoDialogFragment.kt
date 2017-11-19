package com.qwert2603.spenddemo.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import java.text.SimpleDateFormat
import java.util.*

class AppInfoDialogFragment : DialogFragment() {

    @Suppress("DEPRECATION")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = Html.fromHtml(getString(R.string.app_info_text_format,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                SimpleDateFormat("d.MM.yyyy", Locale.getDefault()).format(Date(BuildConfig.BIULD_TIME)),
                SimpleDateFormat("H:mm", Locale.getDefault()).format(Date(BuildConfig.BIULD_TIME))
        ))
        return AlertDialog.Builder(context!!)
                .setMessage(message)
                .setPositiveButton(R.string.text_ok, null)
                .create()
    }

}