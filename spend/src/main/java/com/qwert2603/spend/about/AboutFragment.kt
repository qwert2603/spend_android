package com.qwert2603.spend.about

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.jakewharton.rxbinding2.view.RxView
import com.qwert2603.andrlib.base.mvi.BaseFragment
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.spend.BuildConfig
import com.qwert2603.spend.R
import com.qwert2603.spend.di.DIHolder
import com.qwert2603.spend.dialogs.NotDeletedRecordsHashDialogFragment
import com.qwert2603.spend.utils.setShowing
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.toolbar_default.*
import java.text.SimpleDateFormat
import java.util.*

class AboutFragment : BaseFragment<AboutViewState, AboutView, AboutPresenter>(), AboutView {

    override fun createPresenter() = DIHolder.diManager
            .presentersCreatorComponent
            .aboutPresenterCreatorComponent()
            .build()
            .createPresenter()

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
                .setView(R.layout.dialog_loading)
                .setCancelable(false)
                .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_about, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setTitle(R.string.fragment_title_about)

        @Suppress("DEPRECATION")
        about_TextView.text = Html.fromHtml(getString(R.string.app_info_text_format,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(BuildConfig.BIULD_TIME)),
                SimpleDateFormat("H:mm", Locale.getDefault()).format(Date(BuildConfig.BIULD_TIME)),
                BuildConfig.BIULD_HASH
        ))

        notDeletedRecordsHash_Button.setOnClickListener {
            NotDeletedRecordsHashDialogFragment().show(requireFragmentManager(), null)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        loadingDialog.setShowing(false, requireContext())
        super.onDestroyView()
    }

    override fun sendDumpClicks(): Observable<Any> = RxView.clicks(sendDump_Button)

    override fun render(vs: AboutViewState) {
        super.render(vs)
        loadingDialog.setShowing(vs.isMakingDump, requireContext())
    }

    override fun executeAction(va: ViewAction) {
        if (va !is AboutViewAction) return
        return when (va) {
            is AboutViewAction.SendSump -> {
                val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID, va.file)
                val intent = Intent(Intent.ACTION_SEND)
                intent.setDataAndType(uri, "application/json")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(intent, ""))
            }
            AboutViewAction.DumpError -> Toast.makeText(requireContext(), "dump error", Toast.LENGTH_SHORT).show()
        }
    }
}