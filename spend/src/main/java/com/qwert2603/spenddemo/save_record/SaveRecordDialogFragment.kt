package com.qwert2603.spenddemo.save_record

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qwert2603.andrlib.base.mvi.BaseDialogFragment
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.util.renderIfChanged
import com.qwert2603.andrlib.util.renderIfChangedTwo
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.model.entity.*
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
        private const val REQUEST_CODE_CATEGORY = 23
        private const val REQUEST_CODE_KIND = 24

        private fun View.resolvedActions(): Observable<Boolean> = Observable.merge(
                RxView.clicks(this.cancel_Button).map { false },
                RxView.clicks(this.accept_Button).map { true }
        )
    }

    @Arg
    lateinit var saveRecordKey: SaveRecordKey

    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .saveRecordPresenterCreatorComponent()
            .saveRecordKey(saveRecordKey)
            .build()
            .createSaveRecordPresenter()

    private lateinit var categoryEditText: UserInputEditText
    private lateinit var kindEditText: UserInputEditText
    private lateinit var valueEditText: UserInputEditText

    private val keyboardManager by lazy { context as KeyboardManager }

    private val onDateSelected = PublishSubject.create<Wrapper<SDate>>()
    private val onTimeSelected = PublishSubject.create<Wrapper<STime>>()
    private val onCategoryUuidSelected = PublishSubject.create<String>()
    private val onCategoryUuidAndKindSelected = PublishSubject.create<Pair<String, String>>()

    private lateinit var dialogView: View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_record, null)
        dialogView.kind_EditText.filters = arrayOf(InputFilter.LengthFilter(Const.MAX_RECORD_KIND_LENGTH))

        categoryEditText = UserInputEditText(dialogView.category_EditText)
        kindEditText = UserInputEditText(dialogView.kind_EditText)
        valueEditText = UserInputEditText(dialogView.value_EditText)

        return AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.button_edit, null)
                .setNegativeButton(R.string.button_cancel, null)
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
            REQUEST_CODE_DATE -> onDateSelected.onNext(data.getIntExtraNullable(DatePickerDialogFragment.DATE_KEY)?.toSDate().wrap())
            REQUEST_CODE_TIME -> onTimeSelected.onNext(data.getIntExtraNullable(TimePickerDialogFragment.TIME_KEY)?.toSTime().wrap())
            REQUEST_CODE_CATEGORY -> onCategoryUuidSelected.onNext(data.getStringExtra(ChooseRecordCategoryDialogFragment.CATEGORY_UUID_KEY))
            REQUEST_CODE_KIND -> onCategoryUuidAndKindSelected.onNext(
                    data
                            .getSerializableExtra(ChooseRecordKindDialogFragment.RESULT_KEY)
                            .let { it as ChooseRecordKindDialogFragment.Result }
                            .let { it.recordCategoryUuid to it.kind }
            )
        }
    }

    override fun categoryNameChanges(): Observable<String> = categoryEditText.userInputs()

    override fun kindChanges(): Observable<String> = kindEditText.userInputs()

    override fun valueChanges(): Observable<Int> = valueEditText.userInputs()
            .mapToInt()

    override fun onDateSelected() = onDateSelected
    override fun onTimeSelected() = onTimeSelected

    override fun onCategoryUuidSelected(): Observable<String> = Observable.merge(
            onCategoryUuidSelected,
            RxAutoCompleteTextView
                    .itemClickEvents(dialogView.category_EditText)
                    .map { it.view().adapter.getItem(it.position()) }
                    .ofType(RecordCategoryAggregation::class.java)
                    .map { it.recordCategory.uuid }
    )

    override fun onCategoryUuidAndKindSelected(): Observable<Pair<String, String>> = Observable.merge(
            onCategoryUuidAndKindSelected,
            RxAutoCompleteTextView
                    .itemClickEvents(dialogView.kind_EditText)
                    .map { it.view().adapter.getItem(it.position()) }
                    .ofType(RecordKindAggregation::class.java)
                    .map { it.recordCategory.uuid to it.kind }
    )

    override fun selectCategoryClicks(): Observable<Any> = RxView.longClicks(dialogView.category_EditText)
    override fun selectKindClicks(): Observable<Any> = RxView.longClicks(dialogView.kind_EditText)
    override fun selectDateClicks(): Observable<Any> = RxView.clicks(dialogView.date_EditText)
    override fun selectTimeClicks(): Observable<Any> = RxView.clicks(dialogView.time_EditText)

    override fun onCategoryInputClicked(): Observable<Any> = RxView.clicks(dialogView.category_EditText)
    override fun onKindInputClicked(): Observable<Any> = RxView.clicks(dialogView.kind_EditText)

    override fun onServerCategoryResolved(): Observable<Boolean> = dialogView.category_Change.resolvedActions()
    override fun onServerKindResolved(): Observable<Boolean> = dialogView.kind_Change.resolvedActions()
    override fun onServerDateResolved(): Observable<Boolean> = dialogView.date_Change.resolvedActions()
    override fun onServerTimeResolved(): Observable<Boolean> = dialogView.time_Change.resolvedActions()
    override fun onServerValueResolved(): Observable<Boolean> = dialogView.value_Change.resolvedActions()

    override fun saveClicks(): Observable<Any> = Observable.merge(
            RxView.clicks(dialog.positiveButton),
            RxTextView.editorActions(dialogView.value_EditText)
                    .filter { currentViewState.isSaveEnable() }
    )

    override fun render(vs: SaveRecordViewState) {
        super.render(vs)

        renderIfChangedTwo({ isNewRecord to recordDraft.recordTypeId }) { (isNewRecord, recordTypeId) ->
            dialogView.dialogTitle_TextView.setText(if (isNewRecord) {
                when (recordTypeId) {
                    Const.RECORD_TYPE_ID_SPEND -> R.string.dialog_title_create_spend
                    Const.RECORD_TYPE_ID_PROFIT -> R.string.dialog_title_create_profit
                    else -> null!!
                }
            } else {
                when (recordTypeId) {
                    Const.RECORD_TYPE_ID_SPEND -> R.string.dialog_title_edit_spend
                    Const.RECORD_TYPE_ID_PROFIT -> R.string.dialog_title_edit_profit
                    else -> null!!
                }
            })
            dialog.positiveButton.setText(
                    if (isNewRecord) R.string.button_create
                    else R.string.button_edit
            )
        }
        categoryEditText.setText(vs.recordDraft.recordCategoryName)
        kindEditText.setText(vs.recordDraft.kind)
        valueEditText.setText(vs.valueString)
        DateTimeTextViews.render(
                dateTextView = dialogView.date_EditText,
                timeTextView = dialogView.time_EditText,
                date = vs.recordDraft.date,
                time = vs.recordDraft.time,
                timePanel = dialogView.time_TextInputLayout
        )

        renderIfChanged({ categorySelected() }) {
            dialogView.category_EditText.setTypeface(null, if (it) Typeface.BOLD else Typeface.NORMAL)
        }

        fun renderServerChange(changePanel: View, changeString: String?, @StringRes titleRes: Int) {
            changePanel.setVisible(changeString != null)
            @Suppress("DEPRECATION")
            if (changeString != null) {
                changePanel.change_TextView.text = Html.fromHtml(getString(titleRes, changeString))
            }
        }

        dialogView.apply {
            renderServerChange(category_Change, vs.serverCategory?.name, R.string.server_change_category_format)
            renderServerChange(kind_Change, vs.serverKind, R.string.server_change_kind_format)
            renderServerChange(value_Change, vs.serverValue?.toPointedString(), R.string.server_change_value_format)
            renderServerChange(date_Change, vs.serverDate?.toFormattedString(resources), R.string.server_change_date_format)
            renderServerChange(
                    time_Change,
                    vs.serverTime?.let { it.t?.toString() ?: getString(R.string.no_time_text) },
                    R.string.server_change_time_format
            )
        }

        dialog.positiveButton.isEnabled = vs.isSaveEnable()
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun executeAction(va: ViewAction) {
        if (va !is SaveRecordViewAction) null!!

        fun EditText.focus() {
            postDelayed({
                if (!isResumed) return@postDelayed
                keyboardManager.showKeyboard(this)
            }, 150)
        }

        when (va) {
            SaveRecordViewAction.FocusOnCategoryInput -> dialogView.category_EditText.focus()
            SaveRecordViewAction.FocusOnKindInput -> dialogView.kind_EditText.focus()
            SaveRecordViewAction.FocusOnValueInput -> dialogView.value_EditText.focus()
            is SaveRecordViewAction.AskToSelectDate -> DatePickerDialogFragmentBuilder(va.date.date, true)
                    .minDate(va.minDate.date)
                    .build()
                    .makeShow(REQUEST_CODE_DATE)
            is SaveRecordViewAction.AskToSelectTime -> TimePickerDialogFragmentBuilder
                    .newTimePickerDialogFragment(va.time.time)
                    .makeShow(REQUEST_CODE_TIME)
            is SaveRecordViewAction.AskToSelectCategory -> ChooseRecordCategoryDialogFragmentBuilder
                    .newChooseRecordCategoryDialogFragment(va.recordTypeId)
                    .makeShow(REQUEST_CODE_CATEGORY)
            is SaveRecordViewAction.AskToSelectKind -> ChooseRecordKindDialogFragmentBuilder
                    .newChooseRecordKindDialogFragment(ChooseRecordKindDialogFragment.Key(va.recordTypeId, va.categoryUuid))
                    .makeShow(REQUEST_CODE_KIND)
            is SaveRecordViewAction.ShowCategorySuggestions -> {
                dialogView.category_EditText.setAdapter(CategorySuggestionAdapter(requireContext(), va.suggestions, va.search))
                dialogView.category_EditText.showDropDown()
            }
            SaveRecordViewAction.HideCategorySuggestions -> dialogView.category_EditText.dismissDropDown()
            is SaveRecordViewAction.ShowKindSuggestions -> {
                dialogView.kind_EditText.setAdapter(KindSuggestionAdapter(requireContext(), va.suggestions, va.search, va.withCategory))
                dialogView.kind_EditText.showDropDown()
            }
            SaveRecordViewAction.HideKindSuggestions -> dialogView.kind_EditText.dismissDropDown()
            is SaveRecordViewAction.EditingRecordDeletedOnServer -> {
                Toast.makeText(requireContext(), when (va.recordTypeId) {
                    Const.RECORD_TYPE_ID_SPEND -> R.string.text_deleted_while_edited_spend
                    Const.RECORD_TYPE_ID_PROFIT -> R.string.text_deleted_while_edited_profit
                    else -> null!!
                }, Toast.LENGTH_SHORT).show()
                dismiss()
            }
            SaveRecordViewAction.EditingRecordNotFound -> {
                Toast.makeText(
                        requireContext(),
                        R.string.text_editing_record_not_found,
                        Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
            SaveRecordViewAction.RerenderAll -> renderAll()
            SaveRecordViewAction.Close -> dismissAllowingStateLoss()
        }.also { }
    }

    private fun DialogFragment.makeShow(requestCode: Int) = this
            .also { it.setTargetFragment(this@SaveRecordDialogFragment, requestCode) }
            .show(this@SaveRecordDialogFragment.fragmentManager, null)
}