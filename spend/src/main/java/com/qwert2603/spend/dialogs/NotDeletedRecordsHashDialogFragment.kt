package com.qwert2603.spend.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spend.R
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.utils.disposeOnPause
import kotlinx.android.synthetic.main.dialog_not_deleted_records_hash.view.*
import org.koin.android.ext.android.inject

class NotDeletedRecordsHashDialogFragment : DialogFragment() {

    private val recordsRepo: RecordsRepo by inject()

    private val uiSchedulerProvider: UiSchedulerProvider by inject()

    private lateinit var dialogView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @SuppressLint("InflateParams")
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_not_deleted_records_hash, null)
        dialogView.hash_TextView.setVisible(false)

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.not_deleted_records_hash)
                .setView(dialogView)
                .setPositiveButton(R.string.button_ok, null)
                .create()
    }

    override fun onResume() {
        super.onResume()

        recordsRepo.getNotDeletedRecordsHash()
                .observeOn(uiSchedulerProvider.ui)
                .doOnNext {
                    dialogView.loading_LinearLayout.setVisible(false)
                    dialogView.hash_TextView.setVisible(true)
                    dialogView.hash_TextView.text = it
                }
                .doOnError { LogUtils.e("NotDeletedRecordsHashDialogFragment getNotDeletedRecordsHash", it) }
                .subscribe()
                .disposeOnPause(this)
    }
}