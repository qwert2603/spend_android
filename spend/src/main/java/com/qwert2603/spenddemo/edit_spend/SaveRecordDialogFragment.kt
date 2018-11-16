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
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_edit_record.view.*
import kotlinx.android.synthetic.main.include_server_change.view.*

@FragmentWithArgs
class SaveRecordDialogFragment : BaseDialogFragment<SaveRecordViewState, SaveRecordView, SaveRecordPresenter>(), SaveRecordView {

    companion object {
        private const val REQUEST_CODE_DATE = 21
        private const val REQUEST_CODE_TIME = 22
        private const val REQUEST_CODE_KIND = 23

        private fun View.resolvedActions(): Observable<Boolean> = Observable.merge(
                RxView.clicks(this.cancel_Button).map { false },
                RxView.clicks(this.accept_Button).map { true }
        )
    }

    @Arg
    lateinit var saveRecordKey: SaveRecordKey

    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .editRecordPresenterCreatorComponent()
            .saveRecordKey(saveRecordKey)
            .build()
            .createEditSpendPresenter()

    private val kindEditText by lazy { UserInputEditText(dialogView.kind_EditText) }
    private val valueEditText by lazy { UserInputEditText(dialogView.value_EditText) }

    private val keyboardManager by lazy { context as KeyboardManager }

    private val onDateSelected = PublishSubject.create<Wrapper<Int>>()
    private val onTimeSelected = PublishSubject.create<Wrapper<Int>>()
    private val onKindSelected = PublishSubject.create<String>()

    private lateinit var dialogView: View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_record, null)

        return AlertDialog.Builder(requireContext())
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
            REQUEST_CODE_DATE -> onDateSelected.onNext(data.getIntExtraNullable(DatePickerDialogFragment.DATE_KEY).wrap())
            REQUEST_CODE_TIME -> onTimeSelected.onNext(data.getIntExtraNullable(TimePickerDialogFragment.TIME_KEY).wrap())
            REQUEST_CODE_KIND -> onKindSelected.onNext(data.getStringExtra(ChooseRecordKindDialogFragment.KIND_KEY))
        }
    }

    override fun viewCreated(): Observable<Any> = Observable.just(Any())

    override fun kindChanges(): Observable<String> = kindEditText.userInputs()

    override fun valueChanges(): Observable<Int> = valueEditText.userInputs()
            .mapToInt()

    override fun onDateSelected() = onDateSelected
    override fun onTimeSelected() = onTimeSelected
    override fun onKindSelected() = onKindSelected

    override fun selectKindClicks(): Observable<Any> = RxView.longClicks(dialogView.kind_EditText)
    override fun selectDateClicks(): Observable<Any> = RxView.clicks(dialogView.date_EditText)
    override fun selectTimeClicks(): Observable<Any> = RxView.clicks(dialogView.time_EditText)

    override fun onServerKindResolved(): Observable<Boolean> = dialogView.kind_Change.resolvedActions()
    override fun onServerDateResolved(): Observable<Boolean> = dialogView.date_Change.resolvedActions()
    override fun onServerTimeResolved(): Observable<Boolean> = dialogView.time_Change.resolvedActions()
    override fun onServerValueResolved(): Observable<Boolean> = dialogView.value_Change.resolvedActions()

    override fun saveClicks(): Observable<Any> = Observable.merge(
            RxView.clicks(dialog.positiveButton),
            RxTextView.editorActions(dialogView.value_EditText)
                    .filter { currentViewState.isSaveEnable }
    )

    override fun render(vs: SaveRecordViewState) {
        super.render(vs)

        renderIfChangedTwo({ isNewRecord to recordDraft.recordTypeId }) { (isNewRecord, recordTypeId) ->
            dialogView.dialogTitle_TextView.setText(if (isNewRecord) {
                when (recordTypeId) {
                    Const.RECORD_TYPE_ID_SPEND -> R.string.create_spend_text
                    Const.RECORD_TYPE_ID_PROFIT -> R.string.create_profit_text
                    else -> null!!
                }
            } else {
                when (recordTypeId) {
                    Const.RECORD_TYPE_ID_SPEND -> R.string.edit_spend_text
                    Const.RECORD_TYPE_ID_PROFIT -> R.string.edit_profit_text
                    else -> null!!
                }
            })
        }
        kindEditText.setText(vs.recordDraft.kind)
        valueEditText.setText(vs.valueString)
        DateTimeTextViews.render(
                dateTextView = dialogView.date_EditText,
                timeTextView = dialogView.time_EditText,
                date = vs.recordDraft.date,
                time = vs.recordDraft.time,
                timePanel = dialogView.time_TextInputLayout
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
            renderServerChange(date_Change, vs.serverDate?.toFormattedDateString(resources), R.string.text_server_change_date_format)
            renderServerChange(
                    time_Change,
                    vs.serverTime?.let { it.t?.toTimeString() ?: getString(R.string.no_time_text) },
                    R.string.text_server_change_time_format
            )
        }

        dialog.positiveButton.isEnabled = vs.isSaveEnable
    }

    override fun executeAction(va: ViewAction) {
        if (va !is SaveRecordViewAction) null!!
        when (va) {
            SaveRecordViewAction.FocusOnKindInput -> keyboardManager.showKeyboard(dialogView.kind_EditText)
            SaveRecordViewAction.FocusOnValueInput -> keyboardManager.showKeyboard(dialogView.value_EditText)
            is SaveRecordViewAction.AskToSelectDate -> DatePickerDialogFragmentBuilder.newDatePickerDialogFragment(va.date, true)
                    .makeShow(REQUEST_CODE_DATE)
            is SaveRecordViewAction.AskToSelectTime -> TimePickerDialogFragmentBuilder.newTimePickerDialogFragment(va.time)
                    .makeShow(REQUEST_CODE_TIME)
            is SaveRecordViewAction.AskToSelectKind -> ChooseRecordKindDialogFragmentBuilder
                    .newChooseRecordKindDialogFragment(va.recordTypeId)
                    .makeShow(REQUEST_CODE_KIND)
            is SaveRecordViewAction.EditingRecordDeletedOnServer -> {
                Toast.makeText(requireContext(), when (va.recordTypeId) {
                    Const.RECORD_TYPE_ID_SPEND -> R.string.text_deleted_while_edited_spend
                    Const.RECORD_TYPE_ID_PROFIT -> R.string.text_deleted_while_edited_profit
                    else -> null!!
                }, Toast.LENGTH_SHORT).show()
                dismiss()
            }
            SaveRecordViewAction.RerenderAll -> renderAll()
            SaveRecordViewAction.Close -> dismissAllowingStateLoss()
        }.also { }
    }

    private fun DialogFragment.makeShow(requestCode: Int? = null) = this
            .also { if (requestCode != null) it.setTargetFragment(this@SaveRecordDialogFragment, requestCode) }
            .show(this@SaveRecordDialogFragment.fragmentManager, null)
}