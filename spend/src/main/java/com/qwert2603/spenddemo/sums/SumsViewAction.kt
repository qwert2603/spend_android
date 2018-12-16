package com.qwert2603.spenddemo.sums

import com.qwert2603.andrlib.base.mvi.ViewAction

sealed class SumsViewAction : ViewAction {
    object RerenderAll : SumsViewAction()
}