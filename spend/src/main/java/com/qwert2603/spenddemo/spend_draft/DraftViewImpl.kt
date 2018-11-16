package com.qwert2603.spenddemo.spend_draft

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.view_spend_draft.view.*

class DraftViewImpl constructor(context: Context, attrs: AttributeSet) : BaseFrameLayout<DraftViewState, DraftView, DraftPresenter>(context, attrs), DraftView, DialogAwareView {

    companion object {
        private const val REQUEST_CODE_DATE = 11
        private const val REQUEST_CODE_TIME = 12
        private const val REQUEST_CODE_KIND = 13
    }

    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .draftPresenterComponentBuilder()
            .build()
            .createDraftPresenter()

    private val kindEditText by lazy { UserInputEditText(kind_EditText) }
    private val valueEditText by lazy { UserInputEditText(value_EditText) }

    private val keyboardManager by lazy { context as KeyboardManager }

    private val onDateSelected = PublishSubject.create<Wrapper<Int>>()
    private val onTimeSelected = PublishSubject.create<Wrapper<Int>>()
    private val onKindSelected = PublishSubject.create<String>()

    override lateinit var dialogShower: DialogAwareView.DialogShower

    init {
        inflate(R.layout.view_spend_draft, attachToRoot = true)
        draft_LinearLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return
        when (requestCode) {
            REQUEST_CODE_DATE -> onDateSelected.onNext(data.getIntExtraNullable(DatePickerDialogFragment.DATE_KEY).wrap())
            REQUEST_CODE_TIME -> onTimeSelected.onNext(data.getIntExtraNullable(TimePickerDialogFragment.TIME_KEY).wrap())
            REQUEST_CODE_KIND -> onKindSelected.onNext(data.getStringExtra(ChooseRecordKindDialogFragment.KIND_KEY))
        }
    }

    override fun viewCreated(): Observable<Any> = Observable.just(Any())

    override fun kingChanges(): Observable<String> = kindEditText.userInputs()

    override fun valueChanges(): Observable<Int> = valueEditText.userInputs()
            .mapToInt()

    override fun saveClicks(): Observable<Any> = Observable.merge(
            RxView.clicks(save_Button),
            RxTextView.editorActions(value_EditText)
    )

    override fun selectDateClicks(): Observable<Any> = RxView.clicks(date_EditText)

    override fun selectTimeClicks(): Observable<Any> = RxView.clicks(time_EditText)

    override fun selectKindClicks(): Observable<Any> = RxView.longClicks(kind_EditText)

    override fun onDateSelected(): Observable<Wrapper<Int>> = onDateSelected

    override fun onTimeSelected(): Observable<Wrapper<Int>> = onTimeSelected

    override fun onKindSelected(): Observable<String> = onKindSelected

    override fun onKindInputClicked(): Observable<Any> = RxView.clicks(kind_EditText)

    override fun onKindSuggestionSelected(): Observable<String> = RxAutoCompleteTextView
            .itemClickEvents(kind_EditText)
            .map { it.view().adapter.getItem(it.position()).toString() }

    override fun render(vs: DraftViewState) {
        super.render(vs)
        kindEditText.setText(vs.recordDraft.kind)
        valueEditText.setText(vs.valueString)

        DateTimeTextViews.render(
                dateTextView = date_EditText,
                timeTextView = time_EditText,
                date = vs.recordDraft.date,
                time = vs.recordDraft.time
        )

        renderIfChanged({ recordDraft.isValid() }) {
            save_Button.isEnabled = it
            save_Button.setColorFilter(resources.color(if (it) R.color.colorAccentDark else R.color.button_disabled))
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun executeAction(va: ViewAction) {
        LogUtils.d("DraftViewImpl executeAction $va")
        if (va !is DraftViewAction) null!!
        when (va) {
            DraftViewAction.FocusOnKindInput -> {
                if (keyboardManager.isKeyBoardShown()) {
                    keyboardManager.showKeyboard(kind_EditText)
                } else {
                    kind_EditText.requestFocus()
                }
            }
            DraftViewAction.FocusOnValueInput -> {
                val value_EditText = value_EditText
                value_EditText.postDelayed({
                    if (!isAttachedToWindow) return@postDelayed
                    keyboardManager.showKeyboard(value_EditText)
                }, 200)
            }
            is DraftViewAction.AskToSelectDate -> DatePickerDialogFragmentBuilder
                    .newDatePickerDialogFragment(va.date, true)
                    .also { dialogShower.showDialog(it, REQUEST_CODE_DATE) }
                    .also { keyboardManager.hideKeyboard() }
            is DraftViewAction.AskToSelectTime -> TimePickerDialogFragmentBuilder
                    .newTimePickerDialogFragment(va.time)
                    .also { dialogShower.showDialog(it, REQUEST_CODE_TIME) }
                    .also { keyboardManager.hideKeyboard() }
            DraftViewAction.AskToSelectKind -> ChooseRecordKindDialogFragmentBuilder
                    .newChooseRecordKindDialogFragment(Const.RECORD_TYPE_ID_SPEND)
                    .also { dialogShower.showDialog(it, REQUEST_CODE_KIND) }
                    .also { keyboardManager.hideKeyboard() }
            is DraftViewAction.ShowKindSuggestions -> {
                kind_EditText.setAdapter(SuggestionAdapter(context, va.suggestions.reversed(), va.search))
                kind_EditText.showDropDown()
            }
            DraftViewAction.HideKindSuggestions -> kind_EditText.dismissDropDown()
            DraftViewAction.RerenderAll -> renderAll()
        }.also { }
    }
}