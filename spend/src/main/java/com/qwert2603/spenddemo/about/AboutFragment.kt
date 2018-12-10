package com.qwert2603.spenddemo.about

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jakewharton.rxbinding2.view.RxView
import com.qwert2603.andrlib.base.mvi.BaseFragment
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.utils.setShowing
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
            container?.inflate(R.layout.fragment_about)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setTitle(R.string.about_text)

        @Suppress("DEPRECATION")
        about_TextView.text = Html.fromHtml(getString(R.string.app_info_text_format,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(BuildConfig.BIULD_TIME)),
                SimpleDateFormat("H:mm", Locale.getDefault()).format(Date(BuildConfig.BIULD_TIME)),
                BuildConfig.BIULD_HASH
        ))
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
                startActivity(Intent.createChooser(intent, ""))
            }
            AboutViewAction.DumpError -> Toast.makeText(requireContext(), "dump error", Toast.LENGTH_SHORT).show()
        }
    }
}