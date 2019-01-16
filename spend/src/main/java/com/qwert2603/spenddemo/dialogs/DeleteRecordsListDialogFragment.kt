package com.qwert2603.spenddemo.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.andrlib.util.toPx
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.records_list_view.RecordsListViewImpl
import com.qwert2603.spenddemo.utils.colorStateList
import com.qwert2603.spenddemo.utils.positiveButton
import java.io.Serializable
import javax.inject.Inject

@FragmentWithArgs
class DeleteRecordsListDialogFragment : DialogFragment() {

    data class Key(val recordUuids: List<String>) : Serializable

    @Arg
    lateinit var key: Key

    @Inject
    lateinit var recordsRepo: RecordsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val recordsListViewImpl = RecordsListViewImpl(requireContext(), key.recordUuids)
        val dp16 = resources.toPx(16)
        recordsListViewImpl.setPadding(dp16, dp16, dp16, dp16)
        recordsListViewImpl.onRenderEmptyListListener = {
            Toast.makeText(requireContext(), R.string.text_all_selected_records_were_deleted, Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_delete_selected_records)
                .setView(recordsListViewImpl)
                .setPositiveButton(R.string.button_delete) { _, _ ->
                    recordsRepo.removeRecords(key.recordUuids)
                }
                .setNegativeButton(R.string.button_cancel, null)
                .create()
    }

    override fun onResume() {
        super.onResume()
        dialog.positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
    }
}