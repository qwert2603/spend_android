package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R

@FragmentWithArgs
class DeleteSpendDialogFragment : DialogFragment() {

    companion object {
        const val ID_KEY = "${BuildConfig.APPLICATION_ID}.ID_KEY"
    }

    @Arg
    var id: Long = 0

    @Arg
    var text: String = ""

    // todo: auto-update on spend-change with highlighting.
    // todo: close dialog, if spend is deleted on server.

    // in DeleteProfitDialogFragment too.
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_spend_text)
                    .setMessage(text)
                    .setPositiveButton(R.string.text_delete) { _, _ ->
                        targetFragment!!.onActivityResult(
                                targetRequestCode,
                                Activity.RESULT_OK,
                                Intent().putExtra(ID_KEY, id)
                        )
                    }
                    .setNegativeButton(R.string.text_cancel, null)
                    .create()
}