package com.qwert2603.spend.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.qwert2603.spend.R
import com.qwert2603.spend.SpendDemoApplication
import kotlinx.android.synthetic.main.dialog_debug.view.*

class DebugDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        @SuppressLint("InflateParams")
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_debug, null)

        dialogView.logs_TextView.text = SpendDemoApplication.debugHolder.log

        return AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("clear") { _, _ -> SpendDemoApplication.debugHolder.clearLog() }
                .setNegativeButton(R.string.button_cancel, null)
                .create()
    }
}