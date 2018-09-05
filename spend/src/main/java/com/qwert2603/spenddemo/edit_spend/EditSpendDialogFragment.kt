package com.qwert2603.spenddemo.edit_spend

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qwert2603.andrlib.base.mvi.BaseDialogFragment
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_edit_spend.view.*
import kotlinx.android.synthetic.main.include_server_change.view.*
import java.util.*

@FragmentWithArgs
class EditSpendDialogFragment : BaseDialogFragment<EditSpendViewState, EditSpendView, EditSpendPresenter>(), EditSpendView {

    companion object {
        const val ID_KEY = "${BuildConfig.APPLICATION_ID}.ID_KEY"
        const val KIND_KEY = "${BuildConfig.APPLICATION_ID}.KIND_KEY"
        const val DATE_KEY = "${BuildConfig.APPLICATION_ID}.DATE_KEY"
        const val TIME_KEY = "${BuildConfig.APPLICATION_ID}.TIME_KEY"
        const val VALUE_KEY = "${BuildConfig.APPLICATION_ID}.VALUE_KEY"

        private const val REQUEST_CODE_DATE = 21
        private const val REQUEST_CODE_TIME = 22
        private const val REQUEST_CODE_KIND = 23

        private fun View.resolvedActions(): Observable<Boolean> = Observable.merge(
                RxView.clicks(this.cancel_Button).map { false },
                RxView.clicks(this.accept_Button).map { true }
        )
    }

    @Arg
    var id: Long = 0

    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .editSpendPresenterCreatorComponent()
            .spendId(id)
            .build()
            .createEditSpendPresenter()

    private val kindEditText by lazy { UserInputEditText(dialogView.kind_EditText) }
    private val valueEditText by lazy { UserInputEditText(dialogView.value_EditText) }

    private val keyboardManager by lazy { context as KeyboardManager }

    private val onDateSelected = PublishSubject.create<Date>()
    private val onTimeSelected = PublishSubject.create<Wrapper<Date>>()
    private val onKindSelected = PublishSubject.create<String>()

    // todo: auto-update on spend-change with highlighting.
    // todo: close dialog, if spend is deleted on server.

    // in AddProfitDialogFragment when editing too.

    private lateinit var dialogView: View


    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_spend, null)

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_spend_text)
                .setView(dialogView)
                .setPositiveButton(R.string.text_edit, null)
                .setNegativeButton(R.string.text_cancel, null)
                .create()
    }

    override fun onResume() {
        super.onResume()
        dialog.positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return
        when (requestCode) {
            REQUEST_CODE_DATE -> onDateSelected.onNext(
                    Date(data.getLongExtraNullable(DatePickerDialogFragment.MILLIS_KEY)!!)
            )
            REQUEST_CODE_TIME -> onTimeSelected.onNext(
                    data
                            .getLongExtraNullable(TimePickerDialogFragment.MILLIS_KEY)
                            ?.let { Date(it) }
                            .wrap()
            )
            REQUEST_CODE_KIND -> onKindSelected.onNext(data.getStringExtra(ChooseSpendKindDialogFragment.KIND_KEY))
        }
    }

    override fun viewCreated(): Observable<Any> = Observable.just(Any())

    override fun kindChanges(): Observable<String> = kindEditText.userInputs()

    override fun valueChanges(): Observable<Int> = valueEditText.userInputs()
            .mapToInt()

    override fun onDateSelected(): Observable<Date> = onDateSelected
    override fun onTimeSelected(): Observable<Wrapper<Date>> = onTimeSelected
    override fun onKindSelected(): Observable<String> = onKindSelected

    override fun selectKindClicks(): Observable<Any> = RxView.longClicks(dialogView.kind_EditText)
    override fun selectDateClicks(): Observable<Any> = RxView.clicks(dialogView.date_EditText)
    override fun selectTimeClicks(): Observable<Any> = RxView.clicks(dialogView.time_EditText)

    override fun onServerKindResolved(): Observable<Boolean> = dialogView.kind_Change.resolvedActions()
    override fun onServerDateResolved(): Observable<Boolean> = dialogView.date_Change.resolvedActions()
    override fun onServerTimeResolved(): Observable<Boolean> = dialogView.time_Change.resolvedActions()
    override fun onServerValueResolved(): Observable<Boolean> = dialogView.value_Change.resolvedActions()

    override fun saveClicks(): Observable<Any> = Observable.merge(
            RxView.clicks((dialog as AlertDialog).positiveButton),
            RxTextView.editorActions(dialogView.value_EditText)
                    .filter { currentViewState.isSaveEnable }
    )

    override fun render(vs: EditSpendViewState) {
        super.render(vs)

        kindEditText.setText(vs.kind)
        valueEditText.setText(vs.valueString)
        DateTimeTextViews.render(
                dateTextView = dialogView.date_EditText,
                timeTextView = dialogView.time_EditText,
                date = vs.date,
                time = vs.time
        )

        fun renderServerChange(changePanel: View, changeString: String?, @StringRes titleRes: Int) {
            changePanel.setVisible(changeString != null)
            @Suppress("DEPRECATION")
            if (changeString != null) {
                changePanel.change_TextView.text = Html.fromHtml(getString(titleRes, changeString))
            }
        }

        dialogView.apply {
            renderServerChange(kind_Change, vs.serverKind, R.string.text_server_change_kind_format)
            renderServerChange(value_Change, vs.serverValue?.toPointedString(), R.string.text_server_change_value_format)
            renderServerChange(date_Change, vs.serverDate?.toFormattedString(resources), R.string.text_server_change_date_format)
            renderServerChange(
                    time_Change,
                    vs.serverTime?.let { it.t?.formatTime() ?: getString(R.string.no_time_text) },
                    R.string.text_server_change_time_format
            )
        }

        (dialog as AlertDialog).positiveButton.isEnabled = vs.isSaveEnable
    }

    override fun executeAction(va: ViewAction) {
        if (va !is EditSpendViewAction) return
        when (va) {
            EditSpendViewAction.FocusOnKindInput -> keyboardManager.showKeyboard(dialogView.kind_EditText)
            EditSpendViewAction.FocusOnValueInput -> keyboardManager.showKeyboard(dialogView.value_EditText)
            is EditSpendViewAction.AskToSelectDate -> DatePickerDialogFragmentBuilder.newDatePickerDialogFragment(va.millis, false)
                    .makeShow(REQUEST_CODE_DATE)
            is EditSpendViewAction.AskToSelectTime -> TimePickerDialogFragmentBuilder.newTimePickerDialogFragment(va.millis)
                    .makeShow(REQUEST_CODE_TIME)
            EditSpendViewAction.AskToSelectKind -> ChooseSpendKindDialogFragment().makeShow(REQUEST_CODE_KIND)
            EditSpendViewAction.EditingSpendDeletedOnServer -> {
                Toast.makeText(requireContext(), R.string.text_deleted_while_edited_spend, Toast.LENGTH_SHORT).show()
                dismiss()
            }
            is EditSpendViewAction.SendResult -> sendResult(va.spend)
        }.also { }
    }

    private fun sendResult(spend: Spend) {
        targetFragment!!.onActivityResult(
                targetRequestCode,
                Activity.RESULT_OK,
                Intent()
                        .putExtra(ID_KEY, spend.id)
                        .putExtra(KIND_KEY, spend.kind)
                        .putExtra(DATE_KEY, spend.date.time)
                        .also { intent -> spend.time?.let { intent.putExtra(TIME_KEY, it.time) } }
                        .putExtra(VALUE_KEY, spend.value)
        )
        dismiss()
    }

    private fun DialogFragment.makeShow(requestCode: Int? = null) = this
            .also { if (requestCode != null) it.setTargetFragment(this@EditSpendDialogFragment, requestCode) }
            .show(this@EditSpendDialogFragment.fragmentManager, null)
}