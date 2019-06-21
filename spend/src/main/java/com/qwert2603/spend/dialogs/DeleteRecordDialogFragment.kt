package com.qwert2603.spend.dialogs

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.Record
import com.qwert2603.spend.model.entity.toFormattedString
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.utils.*
import io.reactivex.Observable
import kotlinx.android.synthetic.main.dialog_delete_record.view.*
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

@FragmentWithArgs
class DeleteRecordDialogFragment : DialogFragment() {

    @Arg
    lateinit var uuid: String

    private val recordsRepo: RecordsRepo by inject()

    private val uiSchedulerProvider: UiSchedulerProvider by inject()

    private lateinit var dialogView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @SuppressLint("InflateParams")
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_record, null)
        return AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.button_delete) { _, _ -> recordsRepo.removeRecords(listOf(uuid)) }
                .setNegativeButton(R.string.button_cancel, null)
                .create()
    }

    override fun onResume() {
        requireDialog().positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))

        val recordChanges = recordsRepo.getRecord(uuid)
                .shareReplayLast()

        recordChanges
                .take(1)
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext {
                    if (it.t == null) {
                        Toast.makeText(requireContext(), R.string.text_deleting_record_not_found, Toast.LENGTH_SHORT).show()
                        dismissAllowingStateLoss()
                    }
                }
                .subscribeUntilPaused()

        recordChanges
                .mapNotNull { it.t?.recordCategory?.recordTypeId }
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext {
                    dialogView.dialogTitle_TextView.setText(when (it) {
                        Const.RECORD_TYPE_ID_SPEND -> R.string.dialog_title_delete_spend
                        Const.RECORD_TYPE_ID_PROFIT -> R.string.dialog_title_delete_profit
                        else -> null!!
                    })
                }
                .subscribeUntilPaused()

        recordChanges
                .buffer(2, 1)
                .mapNotNull { (prev, current) ->
                    if (prev.t != null && current.t == null) {
                        prev.t.recordCategory.recordTypeId
                    } else {
                        null
                    }
                }
                .take(1)
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext {
                    Toast.makeText(requireContext(), when (it) {
                        Const.RECORD_TYPE_ID_SPEND -> R.string.text_deleted_while_deleted_spend
                        Const.RECORD_TYPE_ID_PROFIT -> R.string.text_deleted_while_deleted_profit
                        else -> null!!
                    }, Toast.LENGTH_SHORT).show()
                    dismissAllowingStateLoss()
                }
                .subscribeUntilPaused()
        recordChanges
                .skip(1)
                .switchMap {
                    Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                            .take(2)
                            .map { it != 0L }
                }
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext { requireDialog().positiveButton.isEnabled = it }
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

        subscribeFieldUpdates({ it.recordCategory.name }, dialogView.category_TextView)
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
            date.toFormattedString(resources),
            time?.toString()
    ).reduce { acc, s -> "$acc $s" }

    private fun <T> Observable<T>.subscribeUntilPaused() = this
            .doOnError { LogUtils.e("", it) }
            .subscribe()
            .disposeOnPause(this@DeleteRecordDialogFragment)
}