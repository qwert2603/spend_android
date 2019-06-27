package com.qwert2603.spend.navigation

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragmentByWho
import java.io.Serializable

data class DialogTarget(
        val fragmentWho: String,
        val requestCode: Int
) : Serializable

fun DialogFragment.onTargetActivityResult(dialogTarget: DialogTarget, result: Intent) {
    requireFragmentManager()
            .findFragmentByWho(dialogTarget.fragmentWho)!!
            .onActivityResult(dialogTarget.requestCode, Activity.RESULT_OK, result)
}