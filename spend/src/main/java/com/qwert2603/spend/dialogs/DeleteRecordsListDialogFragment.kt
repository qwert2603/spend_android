package com.qwert2603.spend.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.andrlib.util.toPx
import com.qwert2603.spend.R
import com.qwert2603.spend.di.DIHolder
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.records_list_view.RecordsListViewImpl
import com.qwert2603.spend.utils.colorStateList
import com.qwert2603.spend.utils.positiveButton
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
        requireDialog().positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
    }
}