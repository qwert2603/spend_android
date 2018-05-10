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
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.utils.toFormattedString
import javax.inject.Inject

@FragmentWithArgs
class DeleteSpendDialogFragment : DialogFragment() {

    companion object {
        const val ID_KEY = "${BuildConfig.APPLICATION_ID}.ID_KEY"
    }

    @Arg
    var id: Long = 0

    @Inject lateinit var spendsRepo: SpendsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val text = spendsRepo.spendsState()
                .blockingFirst()
                .spends
                .single { it.id == id }
                .let { "${it.date.toFormattedString(resources)}\n${it.kind}\n${it.value}" }
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_spend_text)
                .setMessage(text)
                .setPositiveButton(R.string.text_delete, { _, _ ->
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(ID_KEY, id)
                    )
                })
                .setNegativeButton(R.string.text_cancel, null)
                .create()
    }
}