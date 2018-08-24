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
import kotlinx.android.synthetic.main.dialog_edit_spend.view.*
import java.util.*

@FragmentWithArgs
class EditSpendDialogFragment : DialogFragment() {

    companion object {
        const val ID_KEY = "${BuildConfig.APPLICATION_ID}.ID_KEY"
        const val KIND_KEY = "${BuildConfig.APPLICATION_ID}.KIND_KEY"
        const val DATE_KEY = "${BuildConfig.APPLICATION_ID}.DATE_KEY"
        const val TIME_KEY = "${BuildConfig.APPLICATION_ID}.TIME_KEY"
        const val VALUE_KEY = "${BuildConfig.APPLICATION_ID}.VALUE_KEY"

        private const val SELECTED_DATE_KEY = "SELECTED_DATE_KEY"
        private const val SELECTED_TIME_KEY = "SELECTED_TIME_KEY"

        private const val REQUEST_CHOOSE_KIND = 1
        private const val REQUEST_DATE = 2
        private const val REQUEST_TIME = 3
    }

    @Arg
    var id: Long = 0
    @Arg
    lateinit var kind: String
    @Arg
    var date: Long = 0
    @Arg(required = false)
    var time: Long? = null
    @Arg
    var value: Int = 0

    private lateinit var dialogView: View

    private var selectedDate by BundleLong(SELECTED_DATE_KEY) { arguments!! }
    private var selectedTime by BundleLongNullable(SELECTED_TIME_KEY) { arguments!! }

    // todo: auto-update on spend-change with highlighting.
    // todo: close dialog, if spend is deleted on server.

    // in AddProfitDialogFragment when editing too.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments = arguments ?: Bundle()
        if (savedInstanceState == null) {
            selectedDate = date
            selectedTime = time
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_spend, null)
        dialogView.apply {
            kind_EditText.setText(kind)
            DateTimeTextViews.render(
                    dialogView.date_EditText,
                    dialogView.time_EditText,
                    Date(selectedDate),
                    selectedTime?.let { Date(it) }
            )
            value_EditText.setText(value.toString())

            kind_EditText.setOnLongClickListener {
                ChooseSpendKindDialogFragment()
                        .also { it.setTargetFragment(this@EditSpendDialogFragment, REQUEST_CHOOSE_KIND) }
                        .show(fragmentManager, "choose_kind")
                true
            }
            date_EditText.setOnClickListener {
                DatePickerDialogFragmentBuilder.newDatePickerDialogFragment(selectedDate, false)
                        .also { it.setTargetFragment(this@EditSpendDialogFragment, REQUEST_DATE) }
                        .show(fragmentManager, "date")
            }
            time_EditText.setOnClickListener {
                TimePickerDialogFragmentBuilder
                        .newTimePickerDialogFragment(selectedTime ?: Date().onlyTime().time)
                        .also { it.setTargetFragment(this@EditSpendDialogFragment, REQUEST_TIME) }
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

            Observable
                    .combineLatest(
                            RxTextView.textChanges(kind_EditText)
                                    .map { it.toString() },
                            RxTextView.textChanges(value_EditText)
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
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_spend_text)
                .setView(dialogView)
                .setPositiveButton(R.string.text_edit) { _, _ -> sendResult() }
                .setNegativeButton(R.string.text_cancel, null)
                .create()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CHOOSE_KIND -> {
                    val kind = data.getStringExtra(ChooseSpendKindDialogFragment.KIND_KEY)
                    dialogView.kind_EditText.setText(kind)
                    dialogView.value_EditText.requestFocus()
                    dialogView.value_EditText.selectEnd()
                }
                REQUEST_DATE -> {
                    selectedDate = data.getLongExtra(DatePickerDialogFragment.MILLIS_KEY, 0)
                    DateTimeTextViews.render(
                            dialogView.date_EditText,
                            dialogView.time_EditText,
                            Date(selectedDate),
                            selectedTime?.let { Date(it) }
                    )
                }
                REQUEST_TIME -> {
                    selectedTime = data.getLongExtraNullable(TimePickerDialogFragment.MILLIS_KEY)
                    DateTimeTextViews.render(
                            dialogView.date_EditText,
                            dialogView.time_EditText,
                            Date(selectedDate),
                            selectedTime?.let { Date(it) }
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
                        .putExtra(ID_KEY, id)
                        .putExtra(KIND_KEY, dialogView.kind_EditText.text.toString())
                        .putExtra(DATE_KEY, selectedDate)
                        .also { intent -> selectedTime?.let { intent.putExtra(TIME_KEY, it) } }
                        .putExtra(VALUE_KEY, dialogView.value_EditText.text.toString().toInt())
        )
    }
}