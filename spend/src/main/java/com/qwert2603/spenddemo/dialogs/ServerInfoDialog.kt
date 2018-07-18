package com.qwert2603.spenddemo.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.ServerInfo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import kotlinx.android.synthetic.main.dialog_server_info.view.*
import javax.inject.Inject

class ServerInfoDialog : DialogFragment() {

    @Inject
    lateinit var userSettingsRepo: UserSettingsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIHolder.diManager.viewsComponent.inject(this)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_server_info, null)
        view.apply {
            val serverInfo = userSettingsRepo.serverInfo
            url_EditText.setText(serverInfo.url)
            user_EditText.setText(serverInfo.user)
            password_EditText.setText(serverInfo.password)

            password_EditText.setOnEditorActionListener { _, _, _ ->
                saveServerInfo(view)
                dismiss()
                true
            }
        }

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.text_server_info)
                .setView(view)
                .setPositiveButton(R.string.text_save) { _, _ -> saveServerInfo(view) }
                .setNegativeButton(R.string.text_cancel, null)
                .create()
    }

    private fun saveServerInfo(view: View) = with(view) {
        userSettingsRepo.serverInfo = ServerInfo(
                url = url_EditText.text.toString(),
                user = user_EditText.text.toString(),
                password = password_EditText.text.toString()
        )
    }
}