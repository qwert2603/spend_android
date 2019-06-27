package com.qwert2603.spend.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.toSDate
import com.qwert2603.spend.navigation.onTargetActivityResult
import com.qwert2603.spend.utils.*
import java.util.*

class DatePickerDialogFragment : DialogFragment() {

    companion object {
        const val DATE_KEY = "DATE_KEY"
    }

    private val args by navArgs<DatePickerDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
                .also { it.timeInMillis = args.date.toDateCalendar().timeInMillis }
        return DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    calendar.year = year
                    calendar.month = month
                    calendar.day = dayOfMonth
                    onTargetActivityResult(args.target, Intent().putExtra(DATE_KEY, calendar.toSDate().date))
                },
                calendar.year,
                calendar.month,
                calendar.day
        ).also { datePickerDialog ->
            if (args.withNow) {
                datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.now_text)) { _, _ ->
                    onTargetActivityResult(args.target, Intent())
                }
            }
            args.minDate?.also { datePickerDialog.datePicker.minDate = it.toDateCalendar().timeInMillis }
            args.maxDate?.also { datePickerDialog.datePicker.maxDate = it.toDateCalendar().timeInMillis }
        }
    }

    override fun onResume() {
        super.onResume()
        requireDialog().positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
        requireDialog().neutralButton.setTextColor(resources.colorStateList(R.color.dialog_neutral_button))
    }
}