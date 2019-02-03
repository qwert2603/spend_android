package com.qwert2603.spend.dialogs

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.toSDate
import com.qwert2603.spend.utils.*
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

    @Arg(required = false)
    var minDate: Int = -1

    @Arg(required = false)
    var maxDate: Int = -1

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
                .also { it.timeInMillis = SDate(date).toDateCalendar().timeInMillis }
        return DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    calendar.year = year
                    calendar.month = month
                    calendar.day = dayOfMonth
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(DATE_KEY, calendar.toSDate().date)
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
            if (minDate > 0) it.datePicker.minDate = SDate(minDate).toDateCalendar().timeInMillis
            if (maxDate > 0) it.datePicker.maxDate = SDate(maxDate).toDateCalendar().timeInMillis
        }
    }

    override fun onResume() {
        super.onResume()
        dialog.positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
        dialog.neutralButton.setTextColor(resources.colorStateList(R.color.dialog_neutral_button))
    }
}