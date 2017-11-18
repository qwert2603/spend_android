package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.hannesdorfmann.fragmentargs.FragmentArgs
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.draft.DraftInteractor
import java.util.*
import javax.inject.Inject

@FragmentWithArgs
class DatePickerDialogFragment : DialogFragment() {

    companion object {
        const val MILLIS_KEY = "${BuildConfig.APPLICATION_ID}.MILLIS_KEY"
    }

    @Arg
    var millis: Long = 0

    @Inject lateinit var draftInteractor: DraftInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance().also { it.timeInMillis = millis }
        return DatePickerDialog(
                context,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val targetFragment = targetFragment
                    if (targetFragment != null) {
                        targetFragment.onActivityResult(
                                targetRequestCode,
                                Activity.RESULT_OK,
                                Intent().putExtra(MILLIS_KEY, calendar.timeInMillis)
                        )
                    } else {
                        draftInteractor.onDateChanged(calendar.time)
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
}