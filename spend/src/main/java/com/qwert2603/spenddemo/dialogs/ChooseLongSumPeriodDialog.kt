package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.RxUtils
import com.qwert2603.spenddemo.utils.disposeOnPause
import com.qwert2603.spenddemo.utils.toPointedString
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.item_sum_variant.view.*
import javax.inject.Inject

@FragmentWithArgs
class ChooseLongSumPeriodDialog : DialogFragment() {
    companion object {
        private val VARIANTS = listOf(0, 1, 2, 3, 5, 7, 10, 14, 15, 21, 30, 42, 60, 90, 120, 182, 365, 1461)

        const val DAYS_KEY = "DAYS_KEY"

        fun variantToString(days: Int, resources: Resources): String = if (days > 0) {
            resources.getQuantityString(R.plurals.days, days, days)
        } else {
            resources.getString(R.string.no_sum_text)
        }
    }

    @Arg
    var selectedDays = 0

    @Inject
    lateinit var recordsRepo: RecordsRepo

    @Inject
    lateinit var userSettingsRepo: UserSettingsRepo

    @Inject
    lateinit var uiSchedulerProvider: UiSchedulerProvider

    @Inject
    lateinit var modelSchedulersProvider: ModelSchedulersProvider

    data class Variant(val days: Int, var sum: Long?)

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val variants = VARIANTS.map { Variant(it, null) }
        val adapter = VariantsAdapter(requireContext(), variants) {
            targetFragment!!.onActivityResult(
                    targetRequestCode,
                    Activity.RESULT_OK,
                    Intent().putExtra(DAYS_KEY, VARIANTS[it])
            )
            dismiss()
        }
        val showProfits = userSettingsRepo.showProfits
        val showSpends = userSettingsRepo.showSpends
        variants
                .filter { it.days > 0 }
                .forEach { variant ->
                    RxUtils.dateChanges()
                            .cast(Any::class.java)
                            .startWith(Any())
                            .switchMap {
                                Observable.combineLatest(
                                        if (showSpends || !showProfits) {
                                            recordsRepo.getSumLastDays(Const.RECORD_TYPE_ID_SPEND, variant.days)
                                        } else {
                                            Observable.just(0L)
                                        },
                                        if (showProfits || !showSpends) {
                                            recordsRepo.getSumLastDays(Const.RECORD_TYPE_ID_PROFIT, variant.days)
                                        } else {
                                            Observable.just(0L)
                                        },
                                        BiFunction { s: Long, p: Long -> p - s }
                                )
                            }
                            .doOnError { LogUtils.e("ChooseLongSumPeriodDialog getSumLastDays", it) }
                            .subscribeOn(modelSchedulersProvider.io)
                            .observeOn(uiSchedulerProvider.ui)
                            .subscribe {
                                variant.sum = it
                                adapter.notifyDataSetChanged()
                            }
                            .disposeOnPause(this)
                }
        return AlertDialog.Builder(requireContext())
                .setSingleChoiceItems(
                        adapter,
                        VARIANTS
                                .indexOfFirst { it == selectedDays }
                                .let { if (it >= 0) it else -1 }
                ) { _, _ -> }
                .setTitle(R.string.title_long_sum_dialog)
                .setNegativeButton(R.string.text_cancel, null)
                .create()
    }

    private class VariantsAdapter(
            context: Context,
            variants: List<Variant>,
            private val onClick: (pos: Int) -> Unit
    ) : ArrayAdapter<Variant>(context, 0, variants) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: parent.inflate(R.layout.item_sum_variant)
            with(view) {
                this.setOnClickListener { onClick(position) }
                variant_RadioButton.isChecked = position == (parent as ListView).checkedItemPosition
                val item = getItem(position)!!
                period_TextView.text = variantToString(item.days, resources)
                if (item.days > 0) {
                    sum_TextView.setVisible(true)
                    sum_TextView.text = resources.getString(
                            R.string.variant_sum_format,
                            item.sum?.toPointedString()
                                    ?: resources.getString(R.string.text_variant_sum_loading)
                    )
                } else {
                    sum_TextView.setVisible(false)
                }
            }
            return view
        }
    }
}