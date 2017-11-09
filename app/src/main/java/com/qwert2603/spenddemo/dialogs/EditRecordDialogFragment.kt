package com.qwert2603.spenddemo.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
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

        private const val REQUEST_CHOOSE_KIND = 1
        private const val REQUEST_DATE = 2
    }

    @Arg(required = false)
    var id: Long = 0
    @Arg(required = false) lateinit var kind: String
    @Arg(required = false)
    var date: Long = 0
    @Arg(required = false)
    var value: Int = 0

    private lateinit var dialogView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_record, null)
        val compositeDisposable = CompositeDisposable()
        dialogView.apply {
            kind_EditText.setText(kind)
            date_EditText.setText(Const.DATE_FORMAT.format(Date(date)))
            value_EditText.setText(value.toString())

            kind_EditText.setOnLongClickListener {
                ChooseKindDialogFragment()
                        .also { it.setTargetFragment(this@EditRecordDialogFragment, REQUEST_CHOOSE_KIND) }
                        .show(fragmentManager, "choose_kind")
                true
            }
            date_EditText.setOnClickListener {
                val time = Const.DATE_FORMAT.parse(date_EditText.text.toString()).time
                DatePickerDialogFragmentBuilder.newDatePickerDialogFragment(time)
                        .also { it.setTargetFragment(this@EditRecordDialogFragment, REQUEST_DATE) }
                        .show(fragmentManager, "date")
            }

            val userInputValueEditText = UserInputEditText(value_EditText)
            userInputValueEditText
                    .userInputs()
                    .mapToInt()
                    .filter { it == 0 }
                    .subscribe { userInputValueEditText.setText("") }
                    .addTo(compositeDisposable)

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
                    .addTo(compositeDisposable)
        }
        return AlertDialog.Builder(context!!)
                .setTitle(R.string.edit_record_text)
                .setView(dialogView)
                .setPositiveButton(R.string.text_confirm, { _, _ ->
                    targetFragment?.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent()
                                    .putExtra(ID_KEY, id)
                                    .putExtra(KIND_KEY, dialogView.kind_EditText.text.toString())
                                    .putExtra(DATE_KEY, Const.DATE_FORMAT.parse(dialogView.date_EditText.text.toString()).time)
                                    .putExtra(VALUE_KEY, dialogView.value_EditText.text.toString().toInt())
                    )
                })
                .setNegativeButton(R.string.text_cancel, null)
                .setOnDismissListener {
                    LogUtils.d("compositeDisposable.dispose()")
                    compositeDisposable.dispose()
                }
                .create()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHOOSE_KIND && resultCode == Activity.RESULT_OK && data != null) {
            val kind = data.getStringExtra(ChooseKindDialogFragment.KIND_KEY)
            dialogView.kind_EditText.setText(kind)
            dialogView.kind_EditText.setSelection(kind.length)
        }
        if (requestCode == REQUEST_DATE && resultCode == Activity.RESULT_OK && data != null) {
            val millis = data.getLongExtra(DatePickerDialogFragment.MILLIS_KEY, 0)
            val dateString = Const.DATE_FORMAT.format(Date(millis))
            dialogView.date_EditText.setText(dateString)
            dialogView.date_EditText.setSelection(dateString.length)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}