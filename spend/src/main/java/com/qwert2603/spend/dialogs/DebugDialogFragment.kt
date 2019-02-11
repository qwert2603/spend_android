package com.qwert2603.spend.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.qwert2603.spend.R
import com.qwert2603.spend.SpendApplication
import kotlinx.android.synthetic.main.dialog_debug.view.*

class DebugDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        @SuppressLint("InflateParams")
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_debug, null)

        dialogView.logs_TextView.text = SpendApplication.debugHolder.log

        return AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("clear") { _, _ -> SpendApplication.debugHolder.clearLog() }
                .setNegativeButton(R.string.button_cancel, null)
                .create()
    }
}