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

@FragmentWithArgs
class ChooseLongSumPeriodDialog : DialogFragment() {
    companion object {
        private val VARIANTS = listOf(0, 1, 2, 3, 5, 7, 10, 14, 15, 21, 30, 60, 90, 120, 182, 365)

        const val DAYS_KEY = "DAYS_KEY"

        fun variantToString(days: Int, resources: Resources): String = if (days > 0) {
            resources.getQuantityString(R.plurals.days, days, days)
        } else {
            resources.getString(R.string.no_sum_text)
        }
    }

    @Arg
    var selectedDays = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(requireContext())
                    .setSingleChoiceItems(
                            VARIANTS
                                    .map { variantToString(it, resources) }
                                    .toTypedArray(),
                            VARIANTS
                                    .indexOfFirst { it == selectedDays }
                                    .let { if (it >= 0) it else -1 }
                    ) { _, which ->
                        targetFragment!!.onActivityResult(
                                targetRequestCode,
                                Activity.RESULT_OK,
                                Intent().putExtra(DAYS_KEY, VARIANTS[which])
                        )
                        dismiss()
                    }
                    .setNegativeButton(R.string.text_cancel, null)
                    .create()
}