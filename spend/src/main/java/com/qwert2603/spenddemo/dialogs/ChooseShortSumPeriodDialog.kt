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
class ChooseShortSumPeriodDialog : DialogFragment() {
    companion object {
        private val VARIANTS = listOf(0, 1, 2, 3, 5, 10, 15, 20, 30, 42, 45, 60, 90, 120, 150, 180, 360, 720, 1440, 1441, 1500, 1502, 1918)

        const val MINUTES_KEY = "MINUTES_KEY"

        fun variantToString(minutes: Int, resources: Resources): String = if (minutes > 0) {
            resources.formatTime(minutes)
        } else {
            resources.getString(R.string.no_sum_text)
        }
    }

    @Arg
    var selectedMinutes = 0

    @Inject
    lateinit var spendsRepo: SpendsRepo
    @Inject
    lateinit var profitsRepo: ProfitsRepo
    @Inject
    lateinit var userSettingsRepo: UserSettingsRepo

    data class Variant(val minutes: Int, var sum: Long?)

    private val minuteChangesEvents = SingleLiveEvent<Unit>()
    private lateinit var minuteChangesJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)

        minuteChangesEvents.value = Unit
        minuteChangesJob = launch(newSingleThreadContext("ChooseShortSumPeriodDialog minuteChangesEvents")) {
            try {
                var prevCalendar = Calendar.getInstance()
                while (isActive) {
                    delay(300, TimeUnit.MILLISECONDS)
                    val currentCalendar = Calendar.getInstance()
                    if (!currentCalendar.minutesEqual(prevCalendar)) launch(UI) { minuteChangesEvents.value = Unit }
                    prevCalendar = currentCalendar
                }
            } finally {
                LogUtils.d("end of ChooseShortSumPeriodDialog#minuteChangesJob")
            }
        }
    }

    override fun onDestroy() {
        launch { minuteChangesJob.cancelAndJoin() }
        super.onDestroy()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val variants = VARIANTS.map { Variant(it, null) }
        val adapter = VariantsAdapter(requireContext(), variants) {
            targetFragment!!.onActivityResult(
                    targetRequestCode,
                    Activity.RESULT_OK,
                    Intent().putExtra(MINUTES_KEY, VARIANTS[it])
            )
            dismiss()
        }
        variants
                .filter { it.minutes > 0 }
                .forEach { variant ->
                    val showProfits = userSettingsRepo.showProfits
                    val showSpends = userSettingsRepo.showSpends
                    minuteChangesEvents
                            .switchMap {
                                combineLatest(
                                        liveDataT = if (showProfits || !showSpends) {
                                            profitsRepo.getSumLastMinutes(variant.minutes)
                                        } else {
                                            LDUtils.just(0L)
                                        },
                                        liveDataU = if (showSpends || !showProfits) {
                                            spendsRepo.getSumLastMinutes(variant.minutes)
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
                                .indexOfFirst { it == selectedMinutes }
                                .let { if (it >= 0) it else -1 }
                ) { _, _ -> }
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
                period_TextView.text = variantToString(item.minutes, resources)
                if (item.minutes > 0) {
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