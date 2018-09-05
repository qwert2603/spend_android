package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.Observer
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
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.*
import kotlinx.android.synthetic.main.item_sum_variant.view.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import java.util.*
import java.util.concurrent.TimeUnit
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
    lateinit var spendsRepo: SpendsRepo
    @Inject
    lateinit var profitsRepo: ProfitsRepo
    @Inject
    lateinit var userSettingsRepo: UserSettingsRepo

    data class Variant(val days: Int, var sum: Long?)

    private val dayChangesEvents = SingleLiveEvent<Unit>()
    private lateinit var dayChangesJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)

        dayChangesEvents.value = Unit
        dayChangesJob = launch(newSingleThreadContext("ChooseLongSumPeriodDialog dayChangesEvents")) {
            try {
                var prevCalendar = Calendar.getInstance()
                while (isActive) {
                    delay(300, TimeUnit.MILLISECONDS)
                    val currentCalendar = Calendar.getInstance()
                    if (!currentCalendar.daysEqual(prevCalendar)) launch(UI) { dayChangesEvents.value = Unit }
                    prevCalendar = currentCalendar
                }
            } finally {
                LogUtils.d("end of ChooseLongSumPeriodDialog#dayChangesJob")
            }
        }
    }

    override fun onDestroy() {
        launch { dayChangesJob.cancelAndJoin() }
        super.onDestroy()
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
        variants
                .filter { it.days > 0 }
                .forEach { variant ->
                    val showProfits = userSettingsRepo.showProfits
                    val showSpends = userSettingsRepo.showSpends
                    dayChangesEvents
                            .switchMap {
                                combineLatest(
                                        liveDataT = if (showProfits || !showSpends) {
                                            profitsRepo.getSumLastDays(variant.days)
                                        } else {
                                            LDUtils.just(0L)
                                        },
                                        liveDataU = if (showSpends || !showProfits) {
                                            spendsRepo.getSumLastDays(variant.days)
                                        } else {
                                            LDUtils.just(0L)
                                        },
                                        combiner = { p, s -> p - s }
                                )
                            }
                            .observe(this, Observer {
                                variant.sum = it
                                adapter.notifyDataSetChanged()
                            })
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
                val item = getItem(position)
                period_TextView.text = variantToString(item.days, resources)
                if (item.days > 0) {
                    sum_TextView.setVisible(true)
                    sum_TextView.text = resources.getString(
                            R.string.variant_sum_format,
                            item.sum?.toPointedString() ?: resources.getString(R.string.text_variant_sum_loading)
                    )
                } else {
                    sum_TextView.setVisible(false)
                }
            }
            return view
        }
    }
}