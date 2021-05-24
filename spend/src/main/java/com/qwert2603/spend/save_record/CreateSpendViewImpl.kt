package com.qwert2603.spend.save_record

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.text.InputFilter
import android.util.AttributeSet
import android.widget.EditText
import androidx.navigation.findNavController
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.longClicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.itemClickEvents
import com.qwert2603.andrlib.base.mvi.BaseFrameLayout
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.color
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.andrlib.util.renderIfChanged
import com.qwert2603.spend.R
import com.qwert2603.spend.dialogs.*
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.navigation.DialogTarget
import com.qwert2603.spend.navigation.KeyboardManager
import com.qwert2603.spend.records_list.RecordsListAnimator
import com.qwert2603.spend.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_spend_draft.*
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf

class CreateSpendViewImpl constructor(context: Context, attrs: AttributeSet) :
        BaseFrameLayout<SaveRecordViewState, SaveRecordView, SaveRecordPresenter>(context, attrs),
        SaveRecordView,
        DialogAwareView,
        RecordsListAnimator.SpendOrigin,
        LayoutContainer,
        KoinComponent {

    companion object {
        private const val REQUEST_CODE_DATE = 11
        private const val REQUEST_CODE_TIME = 12
        private const val REQUEST_CODE_CATEGORY = 13
        private const val REQUEST_CODE_KIND = 14
    }

    override val containerView = this

    override fun createPresenter() = get<SaveRecordPresenter> { parametersOf(SaveRecordKey.NewRecord(Const.RECORD_TYPE_ID_SPEND)) }

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
        value_EditText.setupForPointedInt()
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return
        when (requestCode) {
            REQUEST_CODE_DATE -> onDateSelected.onNext(data.getIntExtraNullable(DatePickerDialogFragment.DATE_KEY)?.toSDate().wrap())
            REQUEST_CODE_TIME -> onTimeSelected.onNext(data.getIntExtraNullable(TimePickerDialogFragment.TIME_KEY)?.toSTime().wrap())
            REQUEST_CODE_CATEGORY -> onCategoryUuidSelected.onNext(data.getStringExtra(ChooseRecordCategoryDialogFragment.CATEGORY_UUID_KEY)!!)
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
            save_Button.clicks().map { },
            value_EditText
                    .editorActions()
                    .filter { currentViewState.isSaveEnable() }
    )

    override fun selectDateClicks(): Observable<Any> = date_EditText.clicks().map { }

    override fun selectTimeClicks(): Observable<Any> = time_EditText.clicks().map { }

    override fun selectCategoryClicks(): Observable<Any> = category_EditText.longClicks().map { }

    override fun selectKindClicks(): Observable<Any> = kind_EditText.longClicks().map { }

    override fun onDateSelected() = onDateSelected

    override fun onTimeSelected() = onTimeSelected

    override fun onCategoryUuidSelected(): Observable<String> = Observable.merge(
            onCategoryUuidSelected,
            category_EditText
                    .itemClickEvents()
                    .map { it.view.adapter.getItem(it.position) }
                    .ofType(RecordCategoryAggregation::class.java)
                    .map { it.recordCategory.uuid }
    )

    override fun onCategoryUuidAndKindSelected(): Observable<Pair<String, String>> = Observable.merge(
            onCategoryUuidAndKindSelected,
            kind_EditText
                    .itemClickEvents()
                    .map { it.view.adapter.getItem(it.position) }
                    .ofType(RecordKindAggregation::class.java)
                    .map { it.recordCategory.uuid to it.kind }
    )

    override fun onCategoryInputClicked(): Observable<Any> = category_EditText.clicks().map { }

    override fun onKindInputClicked(): Observable<Any> = kind_EditText.clicks().map { }

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
            save_Button.setColorFilter(resources.color(if (it) R.color.colorPrimary else R.color.button_disabled))
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun executeAction(va: ViewAction) {
        LogUtils.d("CreateSpendViewImpl executeAction $va")
        if (va !is SaveRecordViewAction) null!!

        fun EditText.focus(withKeyboard: Boolean) {
            postDelayed({
                if (!isAttachedToWindow) return@postDelayed
                if (withKeyboard) {
                    keyboardManager.showKeyboard(this)
                } else {
                    this@focus.requestFocus()
                }
            }, 150)
        }

        when (va) {
            SaveRecordViewAction.FocusOnCategoryInput -> category_EditText.focus(false)
            SaveRecordViewAction.FocusOnKindInput -> kind_EditText.focus(true)
            SaveRecordViewAction.FocusOnValueInput -> value_EditText.focus(true)
            is SaveRecordViewAction.AskToSelectDate -> findNavController().navigateFixed(
                    R.id.datePickerDialogFragment,
                    DatePickerDialogFragmentArgs(
                            date = va.date,
                            withNow = true,
                            minDate = va.minDate,
                            maxDate = null,
                            target = DialogTarget(dialogShower.fragmentWho, REQUEST_CODE_DATE)
                    ).toBundle()
            )
            is SaveRecordViewAction.AskToSelectTime -> findNavController().navigateFixed(
                    R.id.timePickerDialogFragment,
                    TimePickerDialogFragmentArgs(
                            va.time,
                            DialogTarget(dialogShower.fragmentWho, REQUEST_CODE_TIME)
                    ).toBundle()
            )
            is SaveRecordViewAction.AskToSelectCategory -> findNavController().navigateFixed(
                    R.id.chooseRecordCategoryDialogFragment,
                    ChooseRecordCategoryDialogFragmentArgs(
                            va.recordTypeId,
                            DialogTarget(dialogShower.fragmentWho, REQUEST_CODE_CATEGORY)
                    ).toBundle()
            )
            is SaveRecordViewAction.AskToSelectKind -> findNavController().navigateFixed(
                    R.id.chooseRecordKindDialogFragment,
                    ChooseRecordKindDialogFragmentArgs(
                            ChooseRecordKindDialogFragment.Key(va.recordTypeId, va.categoryUuid),
                            DialogTarget(dialogShower.fragmentWho, REQUEST_CODE_KIND)
                    ).toBundle()
            )
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

    override fun getDateGlobalVisibleRect(): Rect = date_EditText.getGlobalVisibleRectRightNow()
    override fun getKindGlobalVisibleRect(): Rect = category_EditText.getGlobalVisibleRectRightNow()
    override fun getValueGlobalVisibleRect(): Rect = value_EditText.getGlobalVisibleRectRightNow()
}