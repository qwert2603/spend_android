package com.qwert2603.spenddemo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.repo.ProfitKindsRepo
import javax.inject.Inject

class ChooseProfitKindDialogFragment : DialogFragment() {

    companion object {
        const val KIND_KEY = "${BuildConfig.APPLICATION_ID}.KIND_KEY"
    }

    @Inject
    lateinit var profitKindsRepo: ProfitKindsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val kinds = profitKindsRepo.getAllKinds().blockingGet().toTypedArray()
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_kind_text)
                .setItems(kinds) { _, which ->
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(KIND_KEY, kinds[which])
                    )
                }
                .create()
    }
}