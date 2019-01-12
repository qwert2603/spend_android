package com.qwert2603.spenddemo.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.jakewharton.rxbinding2.widget.RxTextView
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.RecordKindAggregation
import com.qwert2603.spenddemo.model.entity.toFormattedString
import com.qwert2603.spenddemo.model.repo.RecordAggregationsRepo
import com.qwert2603.spenddemo.utils.disposeOnPause
import com.qwert2603.spenddemo.utils.toPointedString
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.dialog_choose_record_kind.view.*
import kotlinx.android.synthetic.main.item_record_kind.view.*
import java.io.Serializable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@FragmentWithArgs
class ChooseRecordKindDialogFragment : DialogFragment() {

    companion object {
        const val RESULT_KEY = "RESULT_KEY"
    }

    data class Key(
            val recordTypeId: Long,
            val recordCategoryUuid: String?
    ) : Serializable

    data class Result(
            val recordCategoryUuid: String,
            val kind: String
    ) : Serializable

    @Arg
    lateinit var key: Key

    @Inject
    lateinit var recordAggregationsRepo: RecordAggregationsRepo

    @Inject
    lateinit var uiSchedulerProvider: UiSchedulerProvider

    private lateinit var dialogView: View

    private lateinit var adapter: RecordKindsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        adapter = RecordKindsAdapter(key.recordCategoryUuid)

        @SuppressLint("InflateParams")
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_choose_record_kind, null)
        dialogView.kinds_RecyclerView.adapter = adapter

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_choose_kind)
                .setView(dialogView)
                .setNegativeButton(R.string.button_cancel, null)
                .create()

    }

    override fun onResume() {
        super.onResume()

        Observable
                .combineLatest(
                        recordAggregationsRepo.getRecordKinds(key.recordTypeId, key.recordCategoryUuid),
                        RxTextView.textChanges(dialogView.search_EditText)
                                .debounce(230, TimeUnit.MILLISECONDS),
                        BiFunction { kinds: List<RecordKindAggregation>, search: CharSequence ->
                            kinds.filter {
                                it.kind.contains(search, ignoreCase = true)
                                        || it.recordCategory.name.contains(search, ignoreCase = true)
                            }
                        }
                )
                .observeOn(uiSchedulerProvider.ui)
                .subscribe {
                    adapter.adapterList = BaseRecyclerViewAdapter.AdapterList(it)
                }
                .disposeOnPause(this)

        RxTextView.textChanges(dialogView.search_EditText)
                .skipInitialValue()
                .subscribe { dialogView.kinds_RecyclerView.scrollToPosition(0) }
                .disposeOnPause(this)

        adapter.modelItemClicks
                .subscribe {
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(RESULT_KEY, Result(
                                    recordCategoryUuid = it.recordCategory.uuid,
                                    kind = it.kind
                            ))
                    )
                    dismiss()
                }
                .disposeOnPause(this)
    }

    private class RecordKindsAdapter(private val categoryUuid: String?) : BaseRecyclerViewAdapter<RecordKindAggregation>() {
        override fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int) = RecordKindViewHolder(parent, categoryUuid)
    }

    private class RecordKindViewHolder(parent: ViewGroup, private val categoryUuid: String?) : BaseRecyclerViewHolder<RecordKindAggregation>(parent, R.layout.item_record_kind) {

        @Suppress("DEPRECATION")
        override fun bind(m: RecordKindAggregation) = with(itemView) {
            super.bind(m)

            val kind = m
            kindName_TextView.text = Html.fromHtml(resources.getString(
                    R.string.record_kind_title_format,
                    if (categoryUuid == null) "${kind.recordCategory.name} / ${kind.kind}" else kind.kind,
                    resources.getQuantityString(R.plurals.times, kind.recordsCount, kind.recordsCount),
                    kind.totalValue.toPointedString()
            ))
            lastRecord_TextView.text = Html.fromHtml(if (kind.lastRecord.time != null) {
                resources.getString(
                        R.string.record_kind_description_format,
                        kind.lastRecord.value.toPointedString(),
                        kind.lastRecord.date.toFormattedString(resources),
                        kind.lastRecord.time.toString()
                )
            } else {
                resources.getString(
                        R.string.record_kind_description_no_time_format,
                        kind.lastRecord.value.toPointedString(),
                        kind.lastRecord.date.toFormattedString(resources)
                )
            })
        }
    }
}