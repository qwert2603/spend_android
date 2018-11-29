package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.RecordCategoryAggregation
import com.qwert2603.spenddemo.model.entity.toFormattedString
import com.qwert2603.spenddemo.model.repo.RecordKindsRepo
import com.qwert2603.spenddemo.utils.disposeOnPause
import com.qwert2603.spenddemo.utils.toPointedString
import kotlinx.android.synthetic.main.item_record_kind.view.*
import javax.inject.Inject

@FragmentWithArgs
class ChooseRecordCategoryDialogFragment : DialogFragment() {

    companion object {
        const val CATEGORY_UUID_KEY = "CATEGORY_UUID_KEY"
    }

    @Arg
    var recordTypeId: Long = IdentifiableLong.NO_ID

    @Inject
    lateinit var recordKindsRepo: RecordKindsRepo

    @Inject
    lateinit var uiSchedulerProvider: UiSchedulerProvider

    private lateinit var recordCategoriesAdapter: RecordCategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // todo: search.
        recordCategoriesAdapter = RecordCategoriesAdapter(requireContext())

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_category_text)
                .setSingleChoiceItems(recordCategoriesAdapter, -1) { _, which ->
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(CATEGORY_UUID_KEY, recordCategoriesAdapter.recordCategories[which].recordCategory.uuid)
                    )
                    dismiss()
                }
                .setNegativeButton(R.string.text_cancel, null)
                .create()

    }

    override fun onResume() {
        super.onResume()

        recordKindsRepo.getRecordCategories(recordTypeId)
                .doOnError { LogUtils.e("ChooseRecordCategoryDialogFragment getRecordCategories", it) }
                .observeOn(uiSchedulerProvider.ui)
                .subscribe {
                    recordCategoriesAdapter.recordCategories = it
                }
                .disposeOnPause(this)
    }

    private class RecordCategoriesAdapter(context: Context) : ArrayAdapter<RecordCategoryAggregation>(context, 0, emptyList()) {

        var recordCategories: List<RecordCategoryAggregation> = emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getCount() = recordCategories.size

        @Suppress("DEPRECATION")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: parent.inflate(R.layout.item_record_kind)
            val category = recordCategories[position]
            view.kindName_TextView.text = Html.fromHtml(view.resources.getString(
                    R.string.record_kind_title_format,
                    category.recordCategory.name,
                    view.resources.getQuantityString(R.plurals.times, category.recordsCount, category.recordsCount),
                    category.totalValue.toPointedString()
            ))
            view.lastRecord_TextView.text = Html.fromHtml(when {
                category.lastRecord == null -> view.resources.getString(R.string.record_kind_description_no_records)
                category.lastRecord.time != null -> view.resources.getString(
                        R.string.record_kind_description_format,
                        category.lastRecord.value.toPointedString(),
                        category.lastRecord.date.toFormattedString(view.resources),
                        category.lastRecord.time.toString()
                )
                else -> view.resources.getString(
                        R.string.record_kind_description_no_time_format,
                        category.lastRecord.value.toPointedString(),
                        category.lastRecord.date.toFormattedString(view.resources)
                )
            })
            return view
        }
    }
}