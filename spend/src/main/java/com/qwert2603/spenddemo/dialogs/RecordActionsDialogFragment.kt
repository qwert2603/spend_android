package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.spenddemo.R
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
        enum class Action(@StringRes val titleRes: Int) : Serializable {
            EDIT(R.string.button_edit),
            DELETE(R.string.button_delete);
        }
    }

    @Arg
    lateinit var recordUuid: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
                .setItems(
                        Result.Action.values()
                                .map { getString(it.titleRes) }
                                .toTypedArray()
                ) { _, which ->
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(RESULT_KEY, Result(recordUuid, Result.Action.values()[which]))
                    )
                }
                .create()
    }
}