package com.qwert2603.spend.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.qwert2603.spend.R
import com.qwert2603.spend.navigation.onTargetActivityResult
import com.qwert2603.spend.records_list_view.RecordsListViewImpl
import com.qwert2603.spend.save_record.SaveRecordKey
import kotlinx.android.synthetic.main.dialog_record_actions.view.*
import java.io.Serializable

class RecordActionsDialogFragment : DialogFragment() {

    companion object {
        const val RESULT_KEY = "RESULT_KEY"
    }

    //todo:remove
    data class Result(
            val recordUuid: String,
            val action: Action
    ) : Serializable {
        enum class Action : Serializable {
            EDIT,
            DELETE;
        }
    }

    private val args by navArgs<RecordActionsDialogFragmentArgs>()

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_record_actions, null)

        dialogView.edit_Button.setOnClickListener {
            findNavController()
                    .navigate(RecordActionsDialogFragmentDirections
                            .actionRecordActionsDialogFragmentToSaveRecordDialogFragment(SaveRecordKey.EditRecord(args.recordUuid)))
        }
        dialogView.delete_Button.setOnClickListener {
            findNavController()
                    .navigate(RecordActionsDialogFragmentDirections
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

    private fun sendResult(action: Result.Action) {
        onTargetActivityResult(
                args.target,
                Intent().putExtra(RESULT_KEY, Result(args.recordUuid, action))
        )
        dismissAllowingStateLoss()
    }
}