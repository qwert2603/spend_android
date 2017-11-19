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
import com.hannesdorfmann.fragmentargs.FragmentArgs
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.utils.UserInputEditText
import com.qwert2603.spenddemo.utils.mapToInt
import com.qwert2603.spenddemo.utils.selectEnd
import com.qwert2603.spenddemo.utils.toFormattedString
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.dialog_edit_record.view.*
import java.util.*

@FragmentWithArgs
class EditRecordDialogFragment : DialogFragment() {
    companion object {
        const val ID_KEY = "${BuildConfig.APPLICATION_ID}.ID_KEY"
        const val KIND_KEY = "${BuildConfig.APPLICATION_ID}.KIND_KEY"
        const val DATE_KEY = "${BuildConfig.APPLICATION_ID}.DATE_KEY"
        const val VALUE_KEY = "${BuildConfig.APPLICATION_ID}.VALUE_KEY"

        private const val SELECTED_DATE_KEY = "SELECTED_DATE_KEY"

        private const val REQUEST_CHOOSE_KIND = 1
        private const val REQUEST_DATE = 2
    }

    @Arg
    var id: Long = 0
    @Arg lateinit var kind: String
    @Arg
    var date: Long = 0
    @Arg
    var value: Int = 0

    private lateinit var dialogView: View

    private var selectedDate: Long
        get() = arguments!!.getLong(SELECTED_DATE_KEY)
        set(value) {
            arguments!!.putLong(SELECTED_DATE_KEY, value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this)
        arguments = arguments ?: Bundle()
        selectedDate = date
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_record, null)
        dialogView.apply {
            kind_EditText.setText(kind)
            date_EditText.setText(Date(selectedDate).toFormattedString(resources))
            value_EditText.setText(value.toString())

            kind_EditText.setOnLongClickListener {
                ChooseKindDialogFragment()
                        .also { it.setTargetFragment(this@EditRecordDialogFragment, REQUEST_CHOOSE_KIND) }
                        .show(fragmentManager, "choose_kind")
                true
            }
            date_EditText.setOnClickListener {
                DatePickerDialogFragmentBuilder.newDatePickerDialogFragment(selectedDate)
                        .also { it.setTargetFragment(this@EditRecordDialogFragment, REQUEST_DATE) }
                        .show(fragmentManager, "date")
            }
            value_EditText.setOnEditorActionListener { _, _, _ ->
                if ((dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE).isEnabled) {
                    sendResult()
                    dismiss()
                }
                true
            }

            val userInputValueEditText = UserInputEditText(value_EditText)
            userInputValueEditText
                    .userInputs()
                    .mapToInt()
                    .filter { it == 0 }
                    .subscribe { userInputValueEditText.setText("") }

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
                    .subscribe({
                        (dialog as? AlertDialog)
                                ?.getButton(AlertDialog.BUTTON_POSITIVE)
                                ?.isEnabled = it
                    })
        }
        return AlertDialog.Builder(context!!)
                .setTitle(R.string.edit_record_text)
                .setView(dialogView)
                .setPositiveButton(R.string.text_edit, { _, _ -> sendResult() })
                .setNegativeButton(R.string.text_cancel, null)
                .create()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHOOSE_KIND && resultCode == Activity.RESULT_OK && data != null) {
            val kind = data.getStringExtra(ChooseKindDialogFragment.KIND_KEY)
            dialogView.kind_EditText.setText(kind)
            dialogView.value_EditText.requestFocus()
            dialogView.value_EditText.selectEnd()
        }
        if (requestCode == REQUEST_DATE && resultCode == Activity.RESULT_OK && data != null) {
            selectedDate = data.getLongExtra(DatePickerDialogFragment.MILLIS_KEY, 0)
            dialogView.date_EditText.setText(Date(selectedDate).toFormattedString(resources))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendResult() {
        targetFragment?.onActivityResult(
                targetRequestCode,
                Activity.RESULT_OK,
                Intent()
                        .putExtra(ID_KEY, id)
                        .putExtra(KIND_KEY, dialogView.kind_EditText.text.toString())
                        .putExtra(DATE_KEY, selectedDate)
                        .putExtra(VALUE_KEY, dialogView.value_EditText.text.toString().toInt())
        )
    }
}