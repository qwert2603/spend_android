package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.model.entity.toSTime
import com.qwert2603.spenddemo.utils.*
import java.util.*

@FragmentWithArgs
class TimePickerDialogFragment : DialogFragment() {

    companion object {
        const val TIME_KEY = "TIME_KEY"
    }

    @Arg
    var time: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
                .also { it.timeInMillis = STime(time).toTimeCalendar().timeInMillis }
        return TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    calendar.hour = h
                    calendar.minute = m
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(TIME_KEY, calendar.toSTime().time)
                    )
                },
                calendar.hour,
                calendar.minute,
                true
        ).also {
            it.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.no_time_text)) { _, _ ->
                targetFragment!!.onActivityResult(
                        targetRequestCode,
                        Activity.RESULT_OK,
                        Intent()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog.positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
        dialog.neutralButton.setTextColor(resources.colorStateList(R.color.dialog_neutral_button))
    }
}