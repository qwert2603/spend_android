package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.utils.Wrapper
import com.qwert2603.spenddemo.utils.toPointedString
import java.util.*

data class EditSpendViewState(
        val id: Long,
        val kind: String,
        val date: Date,
        val time: Date?,
        val value: Int,
        val serverKind: String?,
        val serverDate: Date?,
        val serverTime: Wrapper<Date?>?,
        val serverValue: Int?,
        val justChangedOnServer: Boolean
) {
    val valueString: String = value.takeIf { it != 0 }?.toPointedString() ?: ""

    private val canSave = kind.isNotBlank() && value > 0
    val isSaveEnable = canSave && !justChangedOnServer

    fun getSpend() = Spend(id, kind, value, date, time)
}