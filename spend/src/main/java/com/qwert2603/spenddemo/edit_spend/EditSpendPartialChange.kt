package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.spenddemo.model.entity.Spend
import java.util.*

sealed class EditSpendPartialChange : PartialChange {
    data class SpendLoaded(val spend: Spend) : EditSpendPartialChange()

    data class KindChanged(val kind: String) : EditSpendPartialChange()
    data class ValueChanged(val value: Int) : EditSpendPartialChange()

    data class KindSelected(val kind: String) : EditSpendPartialChange()
    data class DateSelected(val date: Date) : EditSpendPartialChange()
    data class TimeSelected(val time: Date?) : EditSpendPartialChange()

    data class KindChangeOnServer(val kind: String) : EditSpendPartialChange()
    data class ValueChangeOnServer(val value: Int) : EditSpendPartialChange()
    data class DateChangeOnServer(val date: Date) : EditSpendPartialChange()
    data class TimeChangeOnServer(val time: Date?) : EditSpendPartialChange()

    data class SpendJustChangedOnServer(val justChanged: Boolean) : EditSpendPartialChange()

    data class KindServerResolved(val acceptFromServer: Boolean) : EditSpendPartialChange()
    data class ValueServerResolved(val acceptFromServer: Boolean) : EditSpendPartialChange()
    data class DateServerResolved(val acceptFromServer: Boolean) : EditSpendPartialChange()
    data class TimeServerResolved(val acceptFromServer: Boolean) : EditSpendPartialChange()

    object CurrentDateChanged : EditSpendPartialChange()
}