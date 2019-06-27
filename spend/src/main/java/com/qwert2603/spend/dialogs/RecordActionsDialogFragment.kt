package com.qwert2603.spend.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.qwert2603.spend.R
import com.qwert2603.spend.records_list_view.RecordsListViewImpl
import com.qwert2603.spend.save_record.SaveRecordKey
import com.qwert2603.spend.utils.navigateFixed
import kotlinx.android.synthetic.main.dialog_record_actions.view.*

class RecordActionsDialogFragment : DialogFragment() {

    private val args by navArgs<RecordActionsDialogFragmentArgs>()

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_record_actions, null)

        dialogView.edit_Button.setOnClickListener {
            findNavController()
                    .navigateFixed(RecordActionsDialogFragmentDirections
                            .actionRecordActionsDialogFragmentToSaveRecordDialogFragment(SaveRecordKey.EditRecord(args.recordUuid)))
        }
        dialogView.delete_Button.setOnClickListener {
            findNavController()
                    .navigateFixed(RecordActionsDialogFragmentDirections
                            .actionRecordActionsDialogFragmentToDeleteRecordDialogFragment(args.recordUuid))
        }

        val recordsListViewImpl = RecordsListViewImpl(requireContext(), listOf(args.recordUuid))
        recordsListViewImpl.onRenderEmptyListListener = {
            Toast.makeText(requireContext(), R.string.text_record_was_deleted, Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }

        dialogView.dialogRecordActions_LinearLayout.addView(recordsListViewImpl, 0)

        return AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
    }
}