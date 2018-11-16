package com.qwert2603.spenddemo.dialogs

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import kotlinx.android.synthetic.main.dialog_delete_record.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@FragmentWithArgs
class DeleteRecordDialogFragment : DialogFragment() {

    @Arg
    lateinit var uuid: String

    @Inject
    lateinit var recordsRepo: RecordsRepo

    @Inject
    lateinit var uiSchedulerProvider: UiSchedulerProvider

    private lateinit var dialogView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIHolder.diManager.viewsComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @SuppressLint("InflateParams")
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_record, null)
        return AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.text_delete) { _, _ -> recordsRepo.removeRecords(listOf(uuid)) }
                .setNegativeButton(R.string.text_cancel, null)
                .create()
    }

    override fun onResume() {
        dialog.positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))

        val recordChanges = recordsRepo.getRecord(uuid)
                .shareReplayLast()

        recordChanges
                .mapNotNull { it.t?.recordTypeId }
                .doOnNext {
                    dialogView.dialogTitle_TextView.setText(when (it) {
                        Const.RECORD_TYPE_ID_SPEND -> R.string.delete_spend_text
                        Const.RECORD_TYPE_ID_PROFIT -> R.string.delete_profit_text
                        else -> null!!
                    })
                }
                .subscribeUntilPaused()

        recordChanges
                .filter { it.t == null }
                .firstOrError()
                .observeOn(uiSchedulerProvider.ui)
                .doOnSuccess {
                    Toast.makeText(requireContext(), when (it.t!!.recordTypeId) {
                        Const.RECORD_TYPE_ID_SPEND -> R.string.text_deleted_while_deleted_spend
                        Const.RECORD_TYPE_ID_PROFIT -> R.string.text_deleted_while_deleted_profit
                        else -> null!!
                    }, Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .toObservable()
                .subscribeUntilPaused()
        recordChanges
                .skip(1)
                .switchMap {
                    Observable.interval(0, 700, TimeUnit.MILLISECONDS)
                            .take(2)
                            .map { it != 0L }
                }
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext { dialog.positiveButton.isEnabled = it }
                .subscribeUntilPaused()

        fun subscribeFieldUpdates(fieldExtractor: (Record) -> String, textView: TextView) {
            var highlightAnimator: Animator? = null
            recordChanges
                    .mapNotNull { it.t?.let(fieldExtractor) }
                    .distinctUntilChanged()
                    .observeOn(uiSchedulerProvider.ui)
                    .doOnNextIndexed { text, index ->
                        textView.text = text
                        if (index > 0) {
                            val animator = AnimatorUtils
                                    .animateHighlight(textView, R.color.colorPrimary)
                                    .animator
                            highlightAnimator?.cancel()
                            highlightAnimator = animator
                            animator.start()
                        }
                    }
                    .doOnDispose { highlightAnimator?.cancel() }
                    .subscribeUntilPaused()
        }

        subscribeFieldUpdates({ it.kind }, dialogView.kind_TextView)
        subscribeFieldUpdates({ it.value.toPointedString() }, dialogView.value_TextView)
        subscribeFieldUpdates({ it.dateTimeString() }, dialogView.date_TextView)

        RxUtils.dateChanges()
                .withLatestFrom(recordChanges, secondOfTwo())
                .mapNotNull { it.t }
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext { dialogView.date_TextView.text = it.dateTimeString() }
                .subscribeUntilPaused()

        super.onResume()
    }

    private fun Record.dateTimeString() = listOfNotNull(
            date.toFormattedDateString(resources),
            time?.toTimeString()
    ).reduce { acc, s -> "$acc $s" }

    private fun <T> Observable<T>.subscribeUntilPaused() = this
            .doOnError { LogUtils.e("", it) }
            .subscribe()
            .disposeOnPause(this@DeleteRecordDialogFragment)
}