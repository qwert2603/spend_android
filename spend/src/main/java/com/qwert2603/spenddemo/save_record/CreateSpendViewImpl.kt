package com.qwert2603.spenddemo.save_record

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.support.v4.app.DialogFragment
import android.text.InputFilter
import android.util.AttributeSet
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qwert2603.andrlib.base.mvi.BaseFrameLayout
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.color
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.view_spend_draft.view.*

class CreateSpendViewImpl constructor(context: Context, attrs: AttributeSet) :
        BaseFrameLayout<SaveRecordViewState, SaveRecordView, SaveRecordPresenter>(context, attrs),
        SaveRecordView,
        DialogAwareView {

    companion object {
        private const val REQUEST_CODE_DATE = 11
        private const val REQUEST_CODE_TIME = 12
        private const val REQUEST_CODE_CATEGORY = 13
        private const val REQUEST_CODE_KIND = 14
    }

    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .saveRecordPresenterCreatorComponent()
            .saveRecordKey(SaveRecordKey.NewRecord(Const.RECORD_TYPE_ID_SPEND))
            .build()
            .createSaveRecordPresenter()

    private val categoryEditText by lazy { UserInputEditText(category_EditText) }
    private val kindEditText by lazy { UserInputEditText(kind_EditText) }
    private val valueEditText by lazy { UserInputEditText(value_EditText) }

    private val keyboardManager by lazy { context as KeyboardManager }

    private val onDateSelected = PublishSubject.create<Wrapper<SDate>>()
    private val onTimeSelected = PublishSubject.create<Wrapper<STime>>()
    private val onCategoryUuidSelected = PublishSubject.create<String>()
    private val onCategoryUuidAndKindSelected = PublishSubject.create<Pair<String, String>>()

    override lateinit var dialogShower: DialogAwareView.DialogShower

    init {
        inflate(R.layout.view_spend_draft, attachToRoot = true)
        draft_LinearLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        kind_EditText.filters = arrayOf(InputFilter.LengthFilter(Const.MAX_RECORD_KIND_LENGTH))
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

    override fun saveClicks(): Observable<Any> = Observable.merge(
            RxView.clicks(save_Button),
            RxTextView.editorActions(value_EditText)
                    .filter { currentViewState.isSaveEnable() }
    )

    override fun selectDateClicks(): Observable<Any> = RxView.clicks(date_EditText)

    override fun selectTimeClicks(): Observable<Any> = RxView.clicks(time_EditText)

    override fun selectCategoryClicks(): Observable<Any> = RxView.longClicks(category_EditText)

    override fun selectKindClicks(): Observable<Any> = RxView.longClicks(kind_EditText)

    override fun onDateSelected() = onDateSelected

    override fun onTimeSelected() = onTimeSelected

    override fun onCategoryUuidSelected(): Observable<String> = Observable.merge(
            onCategoryUuidSelected,
            RxAutoCompleteTextView
                    .itemClickEvents(category_EditText)
                    .map { it.view().adapter.getItem(it.position()) }
                    .ofType(RecordCategoryAggregation::class.java)
                    .map { it.recordCategory.uuid }
    )

    override fun onCategoryUuidAndKindSelected(): Observable<Pair<String, String>> = Observable.merge(
            onCategoryUuidAndKindSelected,
            RxAutoCompleteTextView
                    .itemClickEvents(kind_EditText)
                    .map { it.view().adapter.getItem(it.position()) }
                    .ofType(RecordKind::class.java)
                    .map { it.recordCategory.uuid to it.kind }
    )

    override fun onCategoryInputClicked(): Observable<Any> = RxView.clicks(category_EditText)

    override fun onKindInputClicked(): Observable<Any> = RxView.clicks(kind_EditText)

    // not for CreateSpendViewImpl.

    override fun onServerCategoryResolved(): Observable<Boolean> = Observable.never()
    override fun onServerKindResolved(): Observable<Boolean> = Observable.never()
    override fun onServerDateResolved(): Observable<Boolean> = Observable.never()
    override fun onServerTimeResolved(): Observable<Boolean> = Observable.never()
    override fun onServerValueResolved(): Observable<Boolean> = Observable.never()

    override fun render(vs: SaveRecordViewState) {
        super.render(vs)
        categoryEditText.setText(vs.recordDraft.recordCategoryName)
        kindEditText.setText(vs.recordDraft.kind)
        valueEditText.setText(vs.valueString)

        DateTimeTextViews.render(
                dateTextView = date_EditText,
                timeTextView = time_EditText,
                date = vs.recordDraft.date,
                time = vs.recordDraft.time
        )

        renderIfChanged({ categorySelected() }) {
            category_EditText.setTypeface(null, if (it) Typeface.BOLD else Typeface.NORMAL)
        }

        renderIfChanged({ isSaveEnable() }) {
            save_Button.isEnabled = it
            save_Button.setColorFilter(resources.color(if (it) R.color.colorAccentDark else R.color.button_disabled))
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun executeAction(va: ViewAction) {
        LogUtils.d("CreateSpendViewImpl executeAction $va")
        if (va !is SaveRecordViewAction) null!!
        when (va) {
            SaveRecordViewAction.FocusOnCategoryInput -> {
                category_EditText.requestFocus()
            }
            SaveRecordViewAction.FocusOnKindInput -> {
                if (keyboardManager.isKeyBoardShown()) {
                    keyboardManager.showKeyboard(kind_EditText)
                } else {
                    kind_EditText.requestFocus()
                }
            }
            SaveRecordViewAction.FocusOnValueInput -> {
                val value_EditText = value_EditText
                value_EditText.postDelayed({
                    if (!isAttachedToWindow) return@postDelayed
                    keyboardManager.showKeyboard(value_EditText)
                }, 200)
            }
            is SaveRecordViewAction.AskToSelectDate -> DatePickerDialogFragmentBuilder
                    .newDatePickerDialogFragment(va.date.date, true)
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
                category_EditText.setAdapter(CategorySuggestionAdapter(context, va.suggestions.reversed(), va.search))
                category_EditText.showDropDown()
            }
            SaveRecordViewAction.HideCategorySuggestions -> category_EditText.dismissDropDown()
            is SaveRecordViewAction.ShowKindSuggestions -> {
                kind_EditText.setAdapter(KindSuggestionAdapter(context, va.suggestions.reversed(), va.search, va.withCategory))
                kind_EditText.showDropDown()
            }
            SaveRecordViewAction.HideKindSuggestions -> kind_EditText.dismissDropDown()
            SaveRecordViewAction.RerenderAll -> renderAll()

            is SaveRecordViewAction.EditingRecordDeletedOnServer,
            SaveRecordViewAction.EditingRecordNotFound,
            SaveRecordViewAction.Close -> Unit // not for CreateSpendViewImpl.
        }.also { }
    }

    private fun DialogFragment.makeShow(requestCode: Int) = this
            .also { dialogShower.showDialog(it, requestCode) }
            .also { keyboardManager.hideKeyboard() }
}