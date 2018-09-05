package com.qwert2603.spenddemo.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.dialog_add_profit.view.*
import java.util.*

@FragmentWithArgs
class AddProfitDialogFragment : DialogFragment() {

    companion object {
        const val ID_KEY = "${BuildConfig.APPLICATION_ID}.ID_KEY"
        const val KIND_KEY = "${BuildConfig.APPLICATION_ID}.KIND_KEY"
        const val DATE_KEY = "${BuildConfig.APPLICATION_ID}.DATE_KEY"
        const val TIME_KEY = "${BuildConfig.APPLICATION_ID}.TIME_KEY"
        const val VALUE_KEY = "${BuildConfig.APPLICATION_ID}.VALUE_KEY"

        private const val SELECTED_DATE_KEY = "SELECTED_DATE_KEY"
        private const val SELECTED_TIME_KEY = "SELECTED_TIME_KEY"

        private const val REQUEST_KIND = 1
        private const val REQUEST_DATE = 2
        private const val REQUEST_TIME = 3
    }

    @Arg(required = true)
    var newProfit: Boolean = true
    @Arg(required = false)
    var id: Long = 0
    @Arg(required = false)
    var kind: String = ""
    @Arg(required = false)
    var date: Long? = null
    @Arg(required = false)
    var time: Long? = null
    @Arg(required = false)
    var value: Int = 0

    private lateinit var dialogView: View

    private var selectedDate by BundleLongNullable(SELECTED_DATE_KEY) { arguments!! }
    private var selectedTime by BundleLongNullable(SELECTED_TIME_KEY) { arguments!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments = arguments ?: Bundle()
        if (!newProfit && savedInstanceState == null) {
            selectedDate = date
            selectedTime = time
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_profit, null)
        dialogView.apply {
            DateTimeTextViews.render(
                    dateTextView = date_EditText,
                    timeTextView = time_EditText,
                    date = selectedDate?.let { Date(it) },
                    time = selectedTime?.let { Date(it) },
                    timePanel = time_TextInputLayout
            )
            if (!newProfit) {
                kind_EditText.setText(kind)
                value_EditText.setText(value.toString())
            }

            kind_EditText.setOnLongClickListener {
                ChooseProfitKindDialogFragment()
                        .also { it.setTargetFragment(this@AddProfitDialogFragment, REQUEST_KIND) }
                        .show(fragmentManager, "choose_kind")
                true
            }
            date_EditText.setOnClickListener {
                DatePickerDialogFragmentBuilder
                        .newDatePickerDialogFragment(
                                selectedDate ?: Date().onlyDate().time,
                                newProfit
                        )
                        .also { it.setTargetFragment(this@AddProfitDialogFragment, REQUEST_DATE) }
                        .show(fragmentManager, "date")
            }
            time_EditText.setOnClickListener {
                TimePickerDialogFragmentBuilder
                        .newTimePickerDialogFragment(selectedTime ?: Date().onlyTime().time)
                        .also { it.setTargetFragment(this@AddProfitDialogFragment, REQUEST_TIME) }
                        .show(fragmentManager, "time")
            }
            value_EditText.setOnEditorActionListener { _, _, _ ->
                if ((dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE).isEnabled) {
                    sendResult()
                    dismiss()
                }
                true
            }

            RxTextView.textChanges(value_EditText)
                    .map { it.toString() }
                    .filter { it.isNotEmpty() && it.all { it == '0' } }
                    .subscribe { value_EditText.setText("") }
        }
        return AlertDialog.Builder(requireContext())
                .setTitle(if (newProfit) R.string.create_profit_text else R.string.edit_profit_text)
                .setView(dialogView)
                .setPositiveButton(if (newProfit) R.string.text_create else R.string.text_edit) { _, _ -> sendResult() }
                .setNegativeButton(R.string.text_cancel, null)
                .create()
                .also {
                    it.setOnShowListener {
                        Observable
                                .combineLatest(
                                        RxTextView.textChanges(dialogView.kind_EditText)
                                                .map { it.toString() },
                                        RxTextView.textChanges(dialogView.value_EditText)
                                                .map { it.toString() }
                                                .mapToInt(),
                                        BiFunction { kind: String, value: Int ->
                                            kind.isNotBlank() && value > 0
                                        }
                                )
                                .subscribe {
                                    (dialog as? AlertDialog)
                                            ?.getButton(AlertDialog.BUTTON_POSITIVE)
                                            ?.isEnabled = it
                                }
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        dialog.positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_KIND -> {
                    val kind = data.getStringExtra(ChooseProfitKindDialogFragment.KIND_KEY)
                    dialogView.kind_EditText.setText(kind)
                    dialogView.value_EditText.requestFocus()
                }
                REQUEST_DATE -> {
                    val dateResult = data.getLongExtraNullable(DatePickerDialogFragment.MILLIS_KEY)
                    if (dateResult == null) {
                        selectedTime = null
                    } else {
                        if (this.selectedDate == null && Date(dateResult).isToday()) {
                            selectedTime = Date().onlyTime().time
                        }
                    }
                    selectedDate = dateResult
                    DateTimeTextViews.render(
                            dateTextView = dialogView.date_EditText,
                            timeTextView = dialogView.time_EditText,
                            date = selectedDate?.let { Date(it) },
                            time = selectedTime?.let { Date(it) },
                            timePanel = dialogView.time_TextInputLayout
                    )
                }
                REQUEST_TIME -> {
                    selectedTime = data.getLongExtraNullable(TimePickerDialogFragment.MILLIS_KEY)
                            .takeIf { selectedDate != null }
                    DateTimeTextViews.render(
                            dateTextView = dialogView.date_EditText,
                            timeTextView = dialogView.time_EditText,
                            date = selectedDate?.let { Date(it) },
                            time = selectedTime?.let { Date(it) },
                            timePanel = dialogView.time_TextInputLayout
                    )
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendResult() {
        targetFragment!!.onActivityResult(
                targetRequestCode,
                Activity.RESULT_OK,
                Intent()
                        .putExtra(KIND_KEY, dialogView.kind_EditText.text.toString())
                        .also { intent -> selectedDate?.let { intent.putExtra(DATE_KEY, it) } }
                        .also { intent -> selectedTime?.let { intent.putExtra(TIME_KEY, it) } }
                        .putExtra(VALUE_KEY, dialogView.value_EditText.text.toString().toInt())
                        .also { if (!newProfit) it.putExtra(ID_KEY, id) }
        )
    }
}