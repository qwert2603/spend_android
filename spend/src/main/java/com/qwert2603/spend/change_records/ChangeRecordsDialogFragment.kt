package com.qwert2603.spend.change_records

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.getWho
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jakewharton.rxbinding3.view.clicks
import com.qwert2603.andrlib.base.mvi.BaseDialogFragment
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.util.color
import com.qwert2603.spend.R
import com.qwert2603.spend.dialogs.DatePickerDialogFragment
import com.qwert2603.spend.dialogs.TimePickerDialogFragment
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.navigation.DialogTarget
import com.qwert2603.spend.records_list_view.RecordsListViewImpl
import com.qwert2603.spend.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_change_records.view.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import java.io.Serializable

class ChangeRecordsDialogFragment : BaseDialogFragment<ChangeRecordsViewState, ChangeRecordsView, ChangeRecordsPresenter>(), ChangeRecordsView {

    companion object {
        private const val REQUEST_CODE_DATE = 31
        private const val REQUEST_CODE_TIME = 32
    }

    data class Key(val recordUuids: List<String>) : Serializable

    private val args by navArgs<ChangeRecordsDialogFragmentArgs>()

    override fun createPresenter() = get<ChangeRecordsPresenter> { parametersOf(args.key.recordUuids) }

    private val changedDateSelected = PublishSubject.create<Wrapper<SDate>>()
    private val changedTimeSelected = PublishSubject.create<Wrapper<Wrapper<STime>>>()

    private lateinit var dialogView: View
    private lateinit var recordsListViewImpl: RecordsListViewImpl

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_records, null)

        dialogView.date_EditText.onRightDrawableClicked { changedDateSelected.onNext(Wrapper(null)) }
        dialogView.time_EditText.onRightDrawableClicked { changedTimeSelected.onNext(Wrapper(null)) }

        recordsListViewImpl = RecordsListViewImpl(requireContext(), args.key.recordUuids)
        recordsListViewImpl.onRenderEmptyListListener = {
            Toast.makeText(requireContext(), R.string.text_all_selected_records_were_deleted, Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }
        recordsListViewImpl.onCanChangeRecords = {
            canChangeRecords = it
        }
        dialogView.dialogChangeRecords_LinearLayout.addView(recordsListViewImpl)

        return AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.button_change, null)
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
            REQUEST_CODE_DATE -> changedDateSelected.onNext(data.getIntExtraNullable(DatePickerDialogFragment.DATE_KEY)?.toSDate().wrap())
            REQUEST_CODE_TIME -> changedTimeSelected.onNext(data.getIntExtraNullable(TimePickerDialogFragment.TIME_KEY)?.toSTime().wrap().wrap())
        }
    }


    override fun askToSelectDateClicks(): Observable<Any> = dialogView.date_EditText.clicks().map { }
    override fun askToSelectTimeClicks(): Observable<Any> = dialogView.time_EditText.clicks().map { }

    override fun changedDateSelected(): Observable<Wrapper<SDate>> = changedDateSelected
    override fun changedTimeSelected(): Observable<Wrapper<Wrapper<STime>>> = changedTimeSelected

    override fun changeClicks(): Observable<Any> = requireDialog().positiveButton.clicks().map { }

    override fun render(vs: ChangeRecordsViewState) {
        super.render(vs)

        fun renderChangeState(textView: TextView, changeText: String?) {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    if (changeText != null) R.drawable.ic_close_black_20dp else 0,
                    0
            )
            textView.setTextColor(resources.color(if (changeText != null) android.R.color.black else R.color.dont_change))
            textView.setTypeface(null, if (changeText != null) Typeface.NORMAL else Typeface.ITALIC)
            textView.text = changeText ?: getString(R.string.dont_change_text)
        }

        dialogView.apply {
            renderChangeState(date_EditText, vs.changedDate?.toFormattedString(resources))
            renderChangeState(time_EditText, vs.changedTime?.let {
                it.t?.toString() ?: getString(R.string.no_time_text)
            })
        }

        isChangeDateAllowed = vs.isChangeEnable()
    }

    override fun executeAction(va: ViewAction) {
        if (va !is ChangeRecordsViewAction) null!!

        return when (va) {
            is ChangeRecordsViewAction.AskToSelectDate -> findNavController()
                    .navigate(ChangeRecordsDialogFragmentDirections.actionChangeRecordsDialogFragmentToDatePickerDialogFragment(
                            date = va.date,
                            withNow = false,
                            minDate = va.minDate,
                            maxDate = null,
                            target = DialogTarget(getWho(), REQUEST_CODE_DATE)
                    ))
            is ChangeRecordsViewAction.AskToSelectTime -> findNavController()
                    .navigate(ChangeRecordsDialogFragmentDirections
                            .actionChangeRecordsDialogFragmentToTimePickerDialogFragment(va.time, DialogTarget(getWho(), REQUEST_CODE_TIME)))
            ChangeRecordsViewAction.RerenderAll -> renderAll()
            ChangeRecordsViewAction.Close -> dismissAllowingStateLoss()
        }
    }

    private var isChangeDateAllowed = false
        set(value) {
            field = value
            updateChangeEnable(isChangeDateAllowed, canChangeRecords)
        }

    private var canChangeRecords = false
        set(value) {
            field = value
            updateChangeEnable(isChangeDateAllowed, canChangeRecords)
        }

    private fun updateChangeEnable(isChangeDateAllowed: Boolean, canChangeRecords: Boolean) {
        requireDialog().positiveButton.isEnabled = isChangeDateAllowed && canChangeRecords
    }
}