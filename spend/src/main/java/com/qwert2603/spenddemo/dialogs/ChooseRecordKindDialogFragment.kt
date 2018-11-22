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
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.RecordKind
import com.qwert2603.spenddemo.model.entity.toFormattedString
import com.qwert2603.spenddemo.model.repo.RecordKindsRepo
import com.qwert2603.spenddemo.utils.disposeOnPause
import com.qwert2603.spenddemo.utils.toPointedString
import kotlinx.android.synthetic.main.item_record_kind.view.*
import javax.inject.Inject

@FragmentWithArgs
class ChooseRecordKindDialogFragment : DialogFragment() {

    companion object {
        const val KIND_KEY = "${BuildConfig.APPLICATION_ID}.KIND_KEY"
    }

    @Arg
    var recordTypeId: Long = IdentifiableLong.NO_ID

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
        recordKindsAdapter = RecordKindsAdapter(requireContext())

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_kind_text)
                .setSingleChoiceItems(recordKindsAdapter, -1) { _, which ->
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(KIND_KEY, recordKindsAdapter.recordKinds[which].kind)
                    )
                    dismiss()
                }
                .setNegativeButton(R.string.text_cancel, null)
                .create()

    }

    override fun onResume() {
        super.onResume()

        recordKindsRepo.getRecordKinds(recordTypeId)
                .doOnError { LogUtils.e("ChooseRecordKindDialogFragment getRecordKinds", it) }
                .observeOn(uiSchedulerProvider.ui)
                .subscribe {
                    recordKindsAdapter.recordKinds = it
                }
                .disposeOnPause(this)
    }

    private class RecordKindsAdapter(context: Context) : ArrayAdapter<RecordKind>(context, 0, emptyList()) {

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
                    kind.kind,
                    view.resources.getQuantityString(R.plurals.times, kind.recordsCount, kind.recordsCount)
            ))
            view.lastRecord_TextView.text = Html.fromHtml(if (kind.lastTime != null) {
                view.resources.getString(
                        R.string.record_kind_description_format,
                        kind.lastValue.toPointedString(),
                        kind.lastDate.toFormattedString(view.resources),
                        kind.lastTime.toString()
                )
            } else {
                view.resources.getString(
                        R.string.record_kind_description_no_time_format,
                        kind.lastValue.toPointedString(),
                        kind.lastDate.toFormattedString(view.resources)
                )
            })
            return view
        }
    }
}