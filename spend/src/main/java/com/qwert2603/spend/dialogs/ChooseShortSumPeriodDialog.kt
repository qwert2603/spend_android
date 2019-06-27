package com.qwert2603.spend.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.Minutes
import com.qwert2603.spend.model.entity.minutes
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.model.repo.UserSettingsRepo
import com.qwert2603.spend.navigation.onTargetActivityResult
import com.qwert2603.spend.utils.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.item_sum_variant.view.*
import org.koin.android.ext.android.inject

class ChooseShortSumPeriodDialog : DialogFragment() {
    companion object {
        private val VARIANTS = listOf(0, 1, 2, 3, 5, 10, 15, 20, 30, 42, 45, 60, 90, 120, 150, 180, 360, 720, 1440, 1441, 1500, 1502, 1918)

        const val MINUTES_KEY = "MINUTES_KEY"

        fun variantToString(minutes: Minutes, resources: Resources): String = if (minutes.minutes > 0) {
            resources.formatTime(minutes)
        } else {
            resources.getString(R.string.no_sum_text)
        }
    }

    private val args by navArgs<ChooseShortSumPeriodDialogArgs>()

    private val recordsRepo: RecordsRepo by inject()

    private val userSettingsRepo: UserSettingsRepo by inject()

    private val uiSchedulerProvider: UiSchedulerProvider by inject()

    data class Variant(val minutes: Minutes, var sum: Long?)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val variants = VARIANTS.map { Variant(it.minutes, null) }
        val adapter = VariantsAdapter(requireContext(), variants) {
            onTargetActivityResult(args.target, Intent().putExtra(MINUTES_KEY, VARIANTS[it].minutes))
            dismissAllowingStateLoss()
        }
        val showProfits = userSettingsRepo.showInfo.field.showProfits
        val showSpends = userSettingsRepo.showInfo.field.showSpends
        variants
                .filter { it.minutes.minutes > 0 }
                .forEach { variant ->
                    RxUtils.minuteChanges()
                            .cast(Any::class.java)
                            .startWith(Any())
                            .switchMap {
                                Observable.combineLatest(
                                        if (showSpends || !showProfits) {
                                            recordsRepo.getSumLastMinutes(Const.RECORD_TYPE_ID_SPEND, variant.minutes)
                                        } else {
                                            Observable.just(0L)
                                        },
                                        if (showProfits || !showSpends) {
                                            recordsRepo.getSumLastMinutes(Const.RECORD_TYPE_ID_PROFIT, variant.minutes)
                                        } else {
                                            Observable.just(0L)
                                        },
                                        BiFunction { s: Long, p: Long -> p - s }
                                )
                            }
                            .doOnError { LogUtils.e("ChooseShortSumPeriodDialog getSumLastMinutes", it) }
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
                                .indexOfFirst { it == args.selected.minutes }
                                .let { if (it >= 0) it else -1 }
                ) { _, _ -> }
                .setTitle(R.string.title_short_sum_dialog)
                .setNegativeButton(R.string.button_cancel, null)
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
                period_TextView.text = variantToString(item.minutes, resources)
                if (item.minutes.minutes > 0) {
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