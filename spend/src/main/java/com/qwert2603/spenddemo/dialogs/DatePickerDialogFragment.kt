package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.utils.*
import java.util.*

@FragmentWithArgs
class DatePickerDialogFragment : DialogFragment() {

    companion object {
        const val DATE_KEY = "DATE_KEY"
    }

    @Arg
    var date: Int = 0

    @Arg
    var withNow: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
                .also { it.timeInMillis = date.toDateCalendar().timeInMillis }
        return DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    calendar.year = year
                    calendar.month = month
                    calendar.day = dayOfMonth
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(DATE_KEY, calendar.toDateInt())
                    )
                },
                calendar.year,
                calendar.month,
                calendar.day
        ).also {
            if (withNow) {
                it.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.now_text)) { _, _ ->
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent()
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog.positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
        dialog.neutralButton.setTextColor(resources.colorStateList(R.color.dialog_neutral_button))
    }
}