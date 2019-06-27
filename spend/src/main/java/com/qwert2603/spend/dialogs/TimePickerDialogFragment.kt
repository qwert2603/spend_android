package com.qwert2603.spend.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.toSTime
import com.qwert2603.spend.navigation.onTargetActivityResult
import com.qwert2603.spend.utils.*
import java.util.*

class TimePickerDialogFragment : DialogFragment() {

    companion object {
        const val TIME_KEY = "TIME_KEY"
    }

    private val args by navArgs<TimePickerDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
                .also { it.timeInMillis = args.time.toTimeCalendar().timeInMillis }
        return TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    calendar.hour = h
                    calendar.minute = m
                    onTargetActivityResult(args.target, Intent().putExtra(TIME_KEY, calendar.toSTime().time))
                },
                calendar.hour,
                calendar.minute,
                true
        ).also {
            it.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.no_time_text)) { _, _ ->
                onTargetActivityResult(args.target, Intent())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireDialog().positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
        requireDialog().neutralButton.setTextColor(resources.colorStateList(R.color.dialog_neutral_button))
    }
}