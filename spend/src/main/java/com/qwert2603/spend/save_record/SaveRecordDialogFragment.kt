package com.qwert2603.spend.save_record

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.longClicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.itemClickEvents
import com.qwert2603.andrlib.base.mvi.BaseDialogFragment
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.util.renderIfChanged
import com.qwert2603.andrlib.util.renderIfChangedTwo
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spend.R
import com.qwert2603.spend.dialogs.*
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.navigation.KeyboardManager
import com.qwert2603.spend.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_edit_record.view.*
import kotlinx.android.synthetic.main.include_server_change.view.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

@FragmentWithArgs
class SaveRecordDialogFragment : BaseDialogFragment<SaveRecordViewState, SaveRecordView, SaveRecordPresenter>(), SaveRecordView {

    companion object {
        private const val REQUEST_CODE_DATE = 21
        private const val REQUEST_CODE_TIME = 22
        private const val REQUEST_CODE_CATEGORY = 23
        private const val REQUEST_CODE_KIND = 24

        private fun View.resolvedActions(): Observable<Boolean> = Observable.merge(
                this.cancel_Button.clicks().map { false },
                this.accept_Button.clicks().map { true }
        )
    }

    @Arg
    lateinit var saveRecordKey: SaveRecordKey

    override fun createPresenter() = get<SaveRecordPresenter> { parametersOf(saveRecordKey) }

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
        dialogView.value_EditText.setupForPointedInt()

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
        requireDialog().positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
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
            dialogView.category_EditText
                    .itemClickEvents()
                    .map { it.view.adapter.getItem(it.position) }
                    .ofType(RecordCategoryAggregation::class.java)
                    .map { it.recordCategory.uuid }
    )

    override fun onCategoryUuidAndKindSelected(): Observable<Pair<String, String>> = Observable.merge(
            onCategoryUuidAndKindSelected,
            dialogView.kind_EditText
                    .itemClickEvents()
                    .map { it.view.adapter.getItem(it.position) }
                    .ofType(RecordKindAggregation::class.java)
                    .map { it.recordCategory.uuid to it.kind }
    )

    override fun selectCategoryClicks(): Observable<Any> = dialogView.category_EditText.longClicks().map { }
    override fun selectKindClicks(): Observable<Any> = dialogView.kind_EditText.longClicks().map { }
    override fun selectDateClicks(): Observable<Any> = dialogView.date_EditText.clicks().map { }
    override fun selectTimeClicks(): Observable<Any> = dialogView.time_EditText.clicks().map { }

    override fun onCategoryInputClicked(): Observable<Any> = dialogView.category_EditText.clicks().map { }
    override fun onKindInputClicked(): Observable<Any> = dialogView.kind_EditText.clicks().map { }

    override fun onServerCategoryResolved(): Observable<Boolean> = dialogView.category_Change.resolvedActions()
    override fun onServerKindResolved(): Observable<Boolean> = dialogView.kind_Change.resolvedActions()
    override fun onServerDateResolved(): Observable<Boolean> = dialogView.date_Change.resolvedActions()
    override fun onServerTimeResolved(): Observable<Boolean> = dialogView.time_Change.resolvedActions()
    override fun onServerValueResolved(): Observable<Boolean> = dialogView.value_Change.resolvedActions()

    override fun saveClicks(): Observable<Any> = Observable.merge(
            requireDialog().positiveButton.clicks().map { },
            dialogView.value_EditText
                    .editorActions()
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
            requireDialog().positiveButton.setText(
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

        requireDialog().positiveButton.isEnabled = vs.isSaveEnable()
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
            .show(this@SaveRecordDialogFragment.requireFragmentManager(), null)
}