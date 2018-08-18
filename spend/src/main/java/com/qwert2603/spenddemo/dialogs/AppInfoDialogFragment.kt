package com.qwert2603.spenddemo.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.utils.Const
import kotlinx.android.synthetic.main.dialog_about.view.*
import java.text.SimpleDateFormat
import java.util.*

class AppInfoDialogFragment : DialogFragment() {

    @SuppressLint("InflateParams")
    @Suppress("DEPRECATION")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_about, null)
        view.about_TextView.text = Html.fromHtml(getString(R.string.app_info_text_format,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(BuildConfig.BIULD_TIME)),
                SimpleDateFormat(Const.TIME_FORMAT_PATTERN, Locale.getDefault()).format(Date(BuildConfig.BIULD_TIME)),
                BuildConfig.BIULD_HASH
        ))
        return AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton(R.string.text_ok, null)
                .create()
    }

}