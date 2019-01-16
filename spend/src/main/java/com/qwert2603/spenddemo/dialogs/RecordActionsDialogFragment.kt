package com.qwert2603.spenddemo.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Toast
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list_view.RecordsListViewImpl
import kotlinx.android.synthetic.main.dialog_record_actions.view.*
import java.io.Serializable

@FragmentWithArgs
class RecordActionsDialogFragment : DialogFragment() {

    companion object {
        const val RESULT_KEY = "RESULT_KEY"
    }

    data class Result(
            val recordUuid: String,
            val action: Action
    ) : Serializable {
        enum class Action : Serializable {
            EDIT,
            DELETE;
        }
    }

    @Arg
    lateinit var recordUuid: String

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_record_actions, null)

        dialogView.edit_Button.setOnClickListener { sendResult(Result.Action.EDIT) }
        dialogView.delete_Button.setOnClickListener { sendResult(Result.Action.DELETE) }

        val recordsListViewImpl = RecordsListViewImpl(requireContext(), listOf(recordUuid))
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
        targetFragment!!.onActivityResult(
                targetRequestCode,
                Activity.RESULT_OK,
                Intent().putExtra(RESULT_KEY, Result(recordUuid, action))
        )
        dismissAllowingStateLoss()
    }
}