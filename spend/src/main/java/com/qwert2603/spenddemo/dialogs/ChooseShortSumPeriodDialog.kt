package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.utils.formatTime

@FragmentWithArgs
class ChooseShortSumPeriodDialog : DialogFragment() {
    companion object {
        private val VARIANTS = listOf(0, 1, 2, 3, 5, 10, 15, 20, 30, 42, 45, 60, 90, 120, 150, 180, 360, 720, 1440, 1441, 1500, 1502, 1918)

        const val MINUTES_KEY = "MINUTES_KEY"

        fun variantToString(minutes: Int, resources: Resources): String = if (minutes > 0) {
            resources.formatTime(minutes)
        } else {
            resources.getString(R.string.no_sum_text)
        }
    }

    @Arg
    var selectedMinutes = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(requireContext())
                    .setSingleChoiceItems(
                            VARIANTS
                                    .map { variantToString(it, resources) }
                                    .toTypedArray(),
                            VARIANTS
                                    .indexOfFirst { it == selectedMinutes }
                                    .let { if (it >= 0) it else -1 }
                    ) { _, which ->
                        targetFragment!!.onActivityResult(
                                targetRequestCode,
                                Activity.RESULT_OK,
                                Intent().putExtra(MINUTES_KEY, VARIANTS[which])
                        )
                        dismiss()
                    }
                    .setNegativeButton(R.string.text_cancel, null)
                    .create()
}