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
import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.RecordCategoryAggregation
import com.qwert2603.spenddemo.model.entity.toFormattedString
import com.qwert2603.spenddemo.model.repo.RecordAggregationsRepo
import com.qwert2603.spenddemo.utils.disposeOnPause
import com.qwert2603.spenddemo.utils.toPointedString
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.dialog_choose_record_category.view.*
import kotlinx.android.synthetic.main.item_record_category.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@FragmentWithArgs
class ChooseRecordCategoryDialogFragment : DialogFragment() {

    companion object {
        const val CATEGORY_UUID_KEY = "CATEGORY_UUID_KEY"
    }

    @Arg
    var recordTypeId: Long = IdentifiableLong.NO_ID

    @Inject
    lateinit var recordAggregationsRepo: RecordAggregationsRepo

    @Inject
    lateinit var uiSchedulerProvider: UiSchedulerProvider

    private lateinit var dialogView: View

    private val adapter = RecordCategoriesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @SuppressLint("InflateParams")
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_choose_record_category, null)
        dialogView.categories_RecyclerView.adapter = adapter

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_choose_category)
                .setView(dialogView)
                .setNegativeButton(R.string.button_cancel, null)
                .create()
    }

    override fun onResume() {
        super.onResume()

        Observable
                .combineLatest(
                        recordAggregationsRepo.getRecordCategories(recordTypeId),
                        RxTextView.textChanges(dialogView.search_EditText)
                                .debounce(230, TimeUnit.MILLISECONDS),
                        BiFunction { categories: List<RecordCategoryAggregation>, search: CharSequence ->
                            categories.filter { it.recordCategory.name.contains(search, ignoreCase = true) }
                        }
                )
                .observeOn(uiSchedulerProvider.ui)
                .subscribe {
                    adapter.adapterList = BaseRecyclerViewAdapter.AdapterList(it)
                }
                .disposeOnPause(this)

        RxTextView.textChanges(dialogView.search_EditText)
                .skipInitialValue()
                .subscribe { dialogView.categories_RecyclerView.scrollToPosition(0) }
                .disposeOnPause(this)

        adapter.modelItemClicks
                .subscribe {
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(CATEGORY_UUID_KEY, it.recordCategory.uuid)
                    )
                    dismiss()
                }
                .disposeOnPause(this)
    }

    private class RecordCategoriesAdapter : BaseRecyclerViewAdapter<RecordCategoryAggregation>() {
        override fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int) = RecordCategoryViewHolder(parent)
    }

    private class RecordCategoryViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<RecordCategoryAggregation>(parent, R.layout.item_record_category) {

        @Suppress("DEPRECATION")
        override fun bind(m: RecordCategoryAggregation) = with(itemView) {
            super.bind(m)

            categoryName_TextView.text = Html.fromHtml(resources.getString(
                    R.string.record_kind_title_format,
                    m.recordCategory.name,
                    resources.getQuantityString(R.plurals.times, m.recordsCount, m.recordsCount),
                    m.totalValue.toPointedString()
            ))
            lastRecord_TextView.text = Html.fromHtml(when {
                m.lastRecord == null -> resources.getString(R.string.record_kind_description_no_records)
                m.lastRecord.time != null -> resources.getString(
                        R.string.record_kind_description_format,
                        m.lastRecord.value.toPointedString(),
                        m.lastRecord.date.toFormattedString(resources),
                        m.lastRecord.time.toString()
                )
                else -> resources.getString(
                        R.string.record_kind_description_no_time_format,
                        m.lastRecord.value.toPointedString(),
                        m.lastRecord.date.toFormattedString(resources)
                )
            })
        }
    }
}