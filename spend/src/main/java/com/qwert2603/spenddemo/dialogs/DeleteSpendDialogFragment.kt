package com.qwert2603.spenddemo.dialogs

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
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.addTo
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.toFormattedString
import com.qwert2603.spenddemo.utils.toPointedString
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_delete.view.*
import java.text.SimpleDateFormat
import java.util.*
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

    @Inject
    lateinit var modelSchedulerProvider: ModelSchedulersProvider

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
                }
                .setNegativeButton(R.string.text_cancel, null)
                .create()
    }

    override fun onResume() {
        val spendChanges = spendsRepo.getSpend(id)
                .subscribeOn(modelSchedulerProvider.io)
                .share()
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

        fun subscribeFieldUpdates(fieldExtractor: (Spend) -> String, textView: TextView) = spendChanges
                .filter { it.t != null }
                .map { fieldExtractor(it.t!!) }
                .distinctUntilChanged()
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext {
                    // todo: highlighting.
                    textView.text = it
                }
                .subscribeUntilPaused()

        subscribeFieldUpdates({ it.kind }, dialogView.kind_TextView)
        subscribeFieldUpdates({ it.value.toPointedString() }, dialogView.value_TextView)
        subscribeFieldUpdates({ spend ->
            listOfNotNull(
                    spend.date.toFormattedString(resources),
                    spend.time?.let { SimpleDateFormat(Const.TIME_FORMAT_PATTERN, Locale.getDefault()).format(it) }
            ).reduce { acc, s -> "$acc $s" }
        }, dialogView.date_TextView)

        super.onResume()
    }

    override fun onPause() {
        resumedDisposable.clear()
        super.onPause()
    }

    private fun <T> Observable<T>.subscribeUntilPaused() = this
            .doOnError { LogUtils.e("", it) }
            .subscribe()
            .addTo(resumedDisposable)
}