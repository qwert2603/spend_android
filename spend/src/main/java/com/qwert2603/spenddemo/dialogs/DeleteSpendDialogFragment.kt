package com.qwert2603.spenddemo.dialogs

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
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
import com.qwert2603.andrlib.util.addTo
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_delete.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@FragmentWithArgs
class DeleteSpendDialogFragment : DialogFragment() {

    companion object {
        const val ID_KEY = "${BuildConfig.APPLICATION_ID}.ID_KEY"
    }

    @Arg
    var id: Long = 0

    @Inject
    lateinit var spendsRepo: SpendsRepo

    @Inject
    lateinit var uiSchedulerProvider: UiSchedulerProvider

    private lateinit var dialogView: View

    private val resumedDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIHolder.diManager.viewsComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @SuppressLint("InflateParams")
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete, null)
        dialogView.dialogTitle_TextView.setText(R.string.delete_spend_text)
        return AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.text_delete) { _, _ ->
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(ID_KEY, id)
                    )
                    resumedDisposable.clear()
                }
                .setNegativeButton(R.string.text_cancel, null)
                .create()
    }

    override fun onResume() {
        dialog.positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))

        val spendChanges = spendsRepo.getSpend(id)
                .shareReplayLast()
        spendChanges
                .filter { it.t == null }
                .firstOrError()
                .observeOn(uiSchedulerProvider.ui)
                .doOnSuccess {
                    Toast.makeText(requireContext(), R.string.text_deleted_while_deleted_spend, Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .toObservable()
                .subscribeUntilPaused()
        spendChanges
                .skip(1)
                .switchMap { _ ->
                    Observable.interval(0, 700, TimeUnit.MILLISECONDS)
                            .take(2)
                            .map { it != 0L }
                }
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext { (dialog as AlertDialog).positiveButton.isEnabled = it }
                .subscribeUntilPaused()

        fun subscribeFieldUpdates(fieldExtractor: (Spend) -> String, textView: TextView) {
            var highlightAnimator: Animator? = null
            spendChanges
                    .filter { it.t != null }
                    .map { fieldExtractor(it.t!!) }
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

        Observable.interval(300, TimeUnit.MILLISECONDS)
                .map { Date().onlyDate() }
                .distinctUntilChanged()
                .skip(1)
                .withLatestFrom(spendChanges, secondOfTwo())
                .mapNotNull { it.t }
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext { dialogView.date_TextView.text = it.dateTimeString() }
                .subscribeUntilPaused()

        super.onResume()
    }

    override fun onPause() {
        resumedDisposable.clear()
        super.onPause()
    }

    private fun Spend.dateTimeString() = listOfNotNull(
            date.toFormattedString(resources),
            time?.let { SimpleDateFormat(Const.TIME_FORMAT_PATTERN, Locale.getDefault()).format(it) }
    ).reduce { acc, s -> "$acc $s" }

    private fun <T> Observable<T>.subscribeUntilPaused() = this
            .doOnError { LogUtils.e("", it) }
            .subscribe()
            .addTo(resumedDisposable)
}