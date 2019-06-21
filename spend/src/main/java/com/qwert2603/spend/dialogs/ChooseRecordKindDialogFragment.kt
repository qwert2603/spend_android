package com.qwert2603.spend.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.jakewharton.rxbinding3.widget.textChanges
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spend.R
import com.qwert2603.spend.model.entity.RecordKindAggregation
import com.qwert2603.spend.model.entity.toFormattedString
import com.qwert2603.spend.model.repo.RecordAggregationsRepo
import com.qwert2603.spend.utils.BundleIntNullable
import com.qwert2603.spend.utils.disposeOnPause
import com.qwert2603.spend.utils.toPointedString
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.dialog_choose_record_kind.view.*
import kotlinx.android.synthetic.main.item_record_kind.view.*
import org.koin.android.ext.android.inject
import java.io.Serializable
import java.util.concurrent.TimeUnit

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

    private val recordAggregationsRepo: RecordAggregationsRepo by inject()

    private val uiSchedulerProvider: UiSchedulerProvider by inject()

    private lateinit var dialogView: View

    private lateinit var adapter: RecordKindsAdapter

    private var firstVisiblePosition by BundleIntNullable("firstVisiblePosition") { arguments!! }

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
                        dialogView.search_EditText
                                .textChanges()
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
                    val prevScroll = firstVisiblePosition
                    if (prevScroll != null) {
                        firstVisiblePosition = null
                        dialogView.kinds_RecyclerView.scrollToPosition(prevScroll)
                    } else {
                        dialogView.kinds_RecyclerView.scrollToPosition(0)
                    }
                }
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

    override fun onPause() {
        super.onPause()
        firstVisiblePosition = (dialogView.kinds_RecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
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