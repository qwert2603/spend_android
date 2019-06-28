package com.qwert2603.spend.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.qwert2603.andrlib.util.toPx
import com.qwert2603.spend.R
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.records_list_view.RecordsListViewImpl
import com.qwert2603.spend.utils.colorStateList
import com.qwert2603.spend.utils.positiveButton
import org.koin.android.ext.android.inject
import java.io.Serializable

class CombineRecordsDialogFragment : DialogFragment() {

    @Keep
    data class Key(
            val recordUuids: List<String>,
            val categoryUuid: String,
            val kind: String
    ) : Serializable

    private val args by navArgs<CombineRecordsDialogFragmentArgs>()

    private val recordsRepo: RecordsRepo by inject()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val recordsListViewImpl = RecordsListViewImpl(requireContext(), args.key.recordUuids)
        val dp16 = resources.toPx(16)
        recordsListViewImpl.setPadding(dp16, dp16, dp16, dp16)
        recordsListViewImpl.onRenderEmptyListListener = {
            Toast.makeText(requireContext(), R.string.text_all_selected_records_were_deleted, Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }
        recordsListViewImpl.onCanChangeRecords = {
            requireDialog().positiveButton.isEnabled = it
        }
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_combine_records)
                .setView(recordsListViewImpl)
                .setPositiveButton(R.string.button_combine) { _, _ ->
                    recordsRepo.combineRecords(
                            recordUuids = args.key.recordUuids,
                            categoryUuid = args.key.categoryUuid,
                            kind = args.key.kind
                    )
                }
                .setNegativeButton(R.string.button_cancel, null)
                .create()
    }

    override fun onResume() {
        super.onResume()
        requireDialog().positiveButton.setTextColor(resources.colorStateList(R.color.dialog_positive_button))
    }
}