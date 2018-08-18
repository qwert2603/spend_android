package com.qwert2603.spenddemo.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.FullSyncStatus
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.toFormattedString
import kotlinx.android.synthetic.main.dialog_last_full_sync.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.qwert2603.andrlib.util.Const as LibConst

class LastFullSyncDialog : DialogFragment() {

    @Inject
    lateinit var spendsRepo: SpendsRepo
    @Inject
    lateinit var profitsRepo: ProfitsRepo
    @Inject
    lateinit var userSettingsRepo: UserSettingsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIHolder.diManager.viewsComponent.inject(this)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_last_full_sync, null)
        spendsRepo.syncStatus().observe(this@LastFullSyncDialog, Observer {
            view.spendsLastSyncTime_TextView.text = getDateString(it!!)
        })
        profitsRepo.syncStatus().observe(this@LastFullSyncDialog, Observer {
            view.profitsLastSyncTime_TextView.text = getDateString(it!!)
        })
        return AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton(R.string.text_ok, null)
                .create()
    }

    private fun getDateString(fullSyncStatus: FullSyncStatus) = when {
        fullSyncStatus.millis == null -> getString(R.string.text_not_sync)
        fullSyncStatus.millis > System.currentTimeMillis() - 1 * LibConst.MILLIS_PER_SECOND -> getString(R.string.now_text)
        else -> getString(
                R.string.last_sync_time_format,
                Date(fullSyncStatus.millis).toFormattedString(resources),
                SimpleDateFormat(Const.TIME_FORMAT_PATTERN, Locale.getDefault()).format(Date(fullSyncStatus.millis))
        )
    }
}