package com.qwert2603.spenddemo.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.SpendKind
import com.qwert2603.spenddemo.model.repo.SpendKindsRepo
import com.qwert2603.spenddemo.utils.toFormattedString
import kotlinx.android.synthetic.main.item_spend_kind.view.*
import javax.inject.Inject

class ChooseSpendKindDialogFragment : DialogFragment() {

    companion object {
        const val KIND_KEY = "${BuildConfig.APPLICATION_ID}.KIND_KEY"
    }

    @Inject
    lateinit var spendKindsRepo: SpendKindsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val kinds = spendKindsRepo.getAllKinds()
                .firstOrError()
                .blockingGet()

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_kind_text)
                .setSingleChoiceItems(SpendKindsAdapter(requireContext(), kinds), -1, { _, which ->
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(KIND_KEY, kinds[which].kind)
                    )
                    dismiss()
                })
                .setNegativeButton(R.string.text_cancel, null)
                .create()

    }

    private class SpendKindsAdapter(context: Context, spendSpendKinds: List<SpendKind>) : ArrayAdapter<SpendKind>(context, 0, spendSpendKinds) {
        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: parent.inflate(R.layout.item_spend_kind)
            val kind = getItem(position)
            view.kindName_TextView.text = "${kind.kind} (${kind.spendsCount})"
            view.lastSpend_TextView.text = "${kind.lastPrice} @ ${kind.lastDate.toFormattedString(view.resources)}"
            return view
        }
    }
}