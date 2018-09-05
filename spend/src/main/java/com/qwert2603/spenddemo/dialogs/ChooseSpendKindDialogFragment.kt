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
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.toFormattedString
import com.qwert2603.spenddemo.utils.toPointedString
import kotlinx.android.synthetic.main.item_spend_kind.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ChooseSpendKindDialogFragment : DialogFragment() {

    companion object {
        const val KIND_KEY = "${BuildConfig.APPLICATION_ID}.KIND_KEY"
        private val TIME_FORMAT = SimpleDateFormat(Const.TIME_FORMAT_PATTERN, Locale.getDefault())
    }

    @Inject
    lateinit var spendKindsRepo: SpendKindsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val kinds = spendKindsRepo.getAllKinds()

        // todo: search.
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_kind_text)
                .setSingleChoiceItems(SpendKindsAdapter(requireContext(), kinds), -1) { _, which ->
                    targetFragment!!.onActivityResult(
                            targetRequestCode,
                            Activity.RESULT_OK,
                            Intent().putExtra(KIND_KEY, kinds[which].kind)
                    )
                    dismiss()
                }
                .setNegativeButton(R.string.text_cancel, null)
                .create()

    }

    private class SpendKindsAdapter(context: Context, spendSpendKinds: List<SpendKind>) : ArrayAdapter<SpendKind>(context, 0, spendSpendKinds) {
        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: parent.inflate(R.layout.item_spend_kind)
            val kind = getItem(position)
            view.kindName_TextView.text = view.resources.getString(
                    R.string.spend_kind_title_format,
                    kind.kind,
                    view.resources.getQuantityString(R.plurals.times, kind.spendsCount, kind.spendsCount)
            )
            // todo: bold via html.
            view.lastSpend_TextView.text = if (kind.lastTime != null) {
                view.resources.getString(
                        R.string.spend_kind_description_format,
                        kind.lastPrice.toLong().toPointedString(),
                        kind.lastDate.toFormattedString(view.resources),
                        TIME_FORMAT.format(kind.lastTime)
                )
            } else {
                view.resources.getString(
                        R.string.spend_kind_description_no_time_format,
                        kind.lastPrice.toLong().toPointedString(),
                        kind.lastDate.toFormattedString(view.resources)
                )
            }
            return view
        }
    }
}