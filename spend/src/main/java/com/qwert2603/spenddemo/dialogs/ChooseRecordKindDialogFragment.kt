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
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.RecordKind
import com.qwert2603.spenddemo.model.entity.toFormattedString
import com.qwert2603.spenddemo.model.repo.RecordKindsRepo
import com.qwert2603.spenddemo.utils.disposeOnPause
import com.qwert2603.spenddemo.utils.toPointedString
import kotlinx.android.synthetic.main.item_record_kind.view.*
import java.io.Serializable
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
    lateinit var recordKindsRepo: RecordKindsRepo

    @Inject
    lateinit var uiSchedulerProvider: UiSchedulerProvider

    private lateinit var recordKindsAdapter: RecordKindsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // todo: search.
        recordKindsAdapter = RecordKindsAdapter(requireContext(), key.recordCategoryUuid)

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_kind_text)
                .setSingleChoiceItems(recordKindsAdapter, -1) { _, which ->
                    val recordKind = recordKindsAdapter.recordKinds[which]
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(RESULT_KEY, Result(
                                    kind = recordKind.kind,
                                    recordCategoryUuid = recordKind.recordCategory.uuid
                            ))
                    )
                    dismiss()
                }
                .setNegativeButton(R.string.text_cancel, null)
                .create()

    }

    override fun onResume() {
        super.onResume()

        recordKindsRepo.getRecordKinds(key.recordTypeId, key.recordCategoryUuid)
                .doOnError { LogUtils.e("ChooseRecordKindDialogFragment getRecordKinds", it) }
                .observeOn(uiSchedulerProvider.ui)
                .subscribe {
                    recordKindsAdapter.recordKinds = it
                }
                .disposeOnPause(this)
    }

    private class RecordKindsAdapter(context: Context, private val categoryUuid: String?) : ArrayAdapter<RecordKind>(context, 0, emptyList()) {

        var recordKinds: List<RecordKind> = emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getCount() = recordKinds.size

        @Suppress("DEPRECATION")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: parent.inflate(R.layout.item_record_kind)
            val kind = recordKinds[position]
            view.kindName_TextView.text = Html.fromHtml(view.resources.getString(
                    R.string.record_kind_title_format,
                    "${kind.recordCategory.name} / ${kind.kind}",
                    view.resources.getQuantityString(R.plurals.times, kind.recordsCount, kind.recordsCount),
                    kind.totalValue.toPointedString()
            ))
            view.lastRecord_TextView.text = Html.fromHtml(if (kind.lastRecord.time != null) {
                view.resources.getString(
                        R.string.record_kind_description_format,
                        kind.lastRecord.value.toPointedString(),
                        kind.lastRecord.date.toFormattedString(view.resources),
                        kind.lastRecord.time.toString()
                )
            } else {
                view.resources.getString(
                        R.string.record_kind_description_no_time_format,
                        kind.lastRecord.value.toPointedString(),
                        kind.lastRecord.date.toFormattedString(view.resources)
                )
            })
            return view
        }
    }
}