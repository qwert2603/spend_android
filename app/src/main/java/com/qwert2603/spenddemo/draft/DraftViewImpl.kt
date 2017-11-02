package com.qwert2603.spenddemo.draft

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import com.hannesdorfmann.mosby3.mvi.layout.MviFrameLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.base_mvi.ViewAction
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.LogUtils
import com.qwert2603.spenddemo.utils.QEditText
import com.qwert2603.spenddemo.utils.inflate
import io.reactivex.Observable
import kotlinx.android.synthetic.main.view_draft.view.*
import java.util.*

class DraftViewImpl @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : MviFrameLayout<DraftView, DraftPresenter>(context, attrs), DraftView {
    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .draftPresenterComponentBuilder()
            .build()
            .createRecordsListPresenter()

    private val kindEditText by lazy { QEditText(kind_EditText) }
    private val valueEditText by lazy { QEditText(value_EditText) }
    private val dateEditText by lazy { QEditText(date_EditText) }

    init {
        inflate(R.layout.view_draft, attachToRoot = true)
    }

    override fun viewCreated(): Observable<Any> = Observable.just(Any())

    override fun kingChanges(): Observable<String> = kindEditText.userInputs()

    override fun valueChanges(): Observable<Int> = valueEditText.userInputs()
            .map {
                try {
                    it.toInt()
                } catch (e: Exception) {
                    LogUtils.e("toInt error!", e)
                    0
                }
            }

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

    override fun render(vs: DraftViewState) {
        LogUtils.d("DraftViewImpl render $vs")
        kindEditText.setText(vs.kind)
        valueEditText.setText(vs.value.let { if (it != 0) it.toString() else "" })
        dateEditText.setText(Const.DATE_FORMAT.format(vs.date))
        save_Button.isEnabled = vs.createEnable
        save_Button.setColorFilter(ResourcesCompat.getColor(
                resources,
                if (vs.createEnable) R.color.colorAccentDark else R.color.button_disabled,
                null
        ))
    }

    override fun executeAction(va: ViewAction) {
        if (va !is DraftViewAction) return
        when (va) {
            is DraftViewAction.FocusOnKindInput -> (context as? KeyboardManager)?.showKeyboard(kind_EditText)
        }
    }
}