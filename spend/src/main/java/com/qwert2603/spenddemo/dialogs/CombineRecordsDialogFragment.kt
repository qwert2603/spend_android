package com.qwert2603.spenddemo.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.records_list_view.RecordsListViewImpl
import com.qwert2603.spenddemo.utils.colorStateList
import com.qwert2603.spenddemo.utils.positiveButton
import java.io.Serializable
import javax.inject.Inject

@FragmentWithArgs
class CombineRecordsDialogFragment : DialogFragment() {

    data class Key(
            val recordUuids: List<String>,
            val categoryUuid: String,
            val kind: String
    ) : Serializable

    @Arg
    lateinit var key: Key

    @Inject
    lateinit var recordsRepo: RecordsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_combine_records)
                .setView(RecordsListViewImpl(requireContext(), key.recordUuids))
                .setPositiveButton(R.string.button_combine) { _, _ ->
                    recordsRepo.combineRecords(
                            recordUuids = key.recordUuids,
                            categoryUuid = key.categoryUuid,
                            kind = key.kind
                    )
                }
                .setNegativeButton(R.string.button_cancel, null)
                .create()
    }

    override fun onResume() {
        super.onResume()
        dialog.positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
    }
}