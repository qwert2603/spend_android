package com.qwert2603.spenddemo.draft

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import com.hannesdorfmann.mosby3.mvi.layout.MviFrameLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.base_mvi.ViewAction
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.ChooseKindDialogFragment
import com.qwert2603.spenddemo.dialogs.DatePickerDialogFragmentBuilder
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import kotlinx.android.synthetic.main.view_draft.view.*
import java.util.*

class DraftViewImpl @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : MviFrameLayout<DraftView, DraftPresenter>(context, attrs), DraftView {
    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .draftPresenterComponentBuilder()
            .build()
            .createRecordsListPresenter()

    private val kindEditText by lazy { UserInputEditText(kind_EditText) }
    private val valueEditText by lazy { UserInputEditText(value_EditText) }
    private val dateEditText by lazy { UserInputEditText(date_EditText) }

    private val keyboardManager by lazy { context as KeyboardManager }

    init {
        inflate(R.layout.view_draft, attachToRoot = true)
    }

    override fun viewCreated(): Observable<Any> = Observable.just(Any())

    override fun kingChanges(): Observable<String> = kindEditText.userInputs()

    override fun valueChanges(): Observable<Int> = valueEditText.userInputs()
            .mapToInt()

    override fun dateChanges(): Observable<Date> = dateEditText.userInputs()
            .map {
                try {
                    Const.DATE_FORMAT.parse(it)
                } catch (e: Exception) {
                    LogUtils.e("parse error!", e)
                    Date(0)
                }
            }

    override fun saveClicks(): Observable<Any> = Observable.merge(
            RxView.clicks(save_Button),
            RxTextView.editorActions(value_EditText)
    )

    override fun selectDateClicks(): Observable<Any> = RxView.clicks(date_EditText)

    override fun selectKindClicks(): Observable<Any> = RxView.longClicks(kind_EditText)

    override fun suggestionSelected(): Observable<String> = RxAutoCompleteTextView
            .itemClickEvents(kind_EditText)
            .map { it.view().adapter.getItem(it.position()).toString() }

    override fun render(vs: DraftViewState) {
        LogUtils.d("DraftViewImpl render $vs")
        kindEditText.setText(vs.creatingRecord.kind)
        valueEditText.setText(vs.valueString)
        dateEditText.setText(vs.dateString)
        date_EditText.setTextColor(resources.color(if (vs.creatingRecord.dateSet) android.R.color.black else R.color.date_default))
        save_Button.isEnabled = vs.createEnable
        save_Button.setColorFilter(resources.color(if (vs.createEnable) R.color.colorAccentDark else R.color.button_disabled))
    }

    override fun executeAction(va: ViewAction) {
        LogUtils.d("DraftViewImpl executeAction $va")
        if (va !is DraftViewAction) return
        when (va) {
            is DraftViewAction.FocusOnKindInput -> {
                if (keyboardManager.isKeyBoardShown()) {
                    keyboardManager.showKeyboard(kind_EditText)
                }
            }
            is DraftViewAction.FocusOnValueInput -> {
                if (keyboardManager.isKeyBoardShown()) {
                    keyboardManager.showKeyboard(value_EditText)
                }
            }
            is DraftViewAction.AskToSelectDate -> DatePickerDialogFragmentBuilder.newDatePickerDialogFragment(va.millis)
                    .show((context as FragmentActivity).supportFragmentManager, "date")
                    .also { keyboardManager.hideKeyboard() }
            is DraftViewAction.AskToSelectKind -> ChooseKindDialogFragment()
                    .show((context as FragmentActivity).supportFragmentManager, "choose_kind")
                    .also { keyboardManager.hideKeyboard() }
            is DraftViewAction.ShowKindSuggestions -> {
                kind_EditText.setAdapter(SuggestionAdapter(context, va.suggestions, va.search))
                kind_EditText.showDropDown()
            }
            is DraftViewAction.HideKindSuggestions -> kind_EditText.dismissDropDown()
        }
    }
}