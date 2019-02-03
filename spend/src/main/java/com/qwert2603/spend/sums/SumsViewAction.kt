package com.qwert2603.spend.sums

import com.qwert2603.andrlib.base.mvi.ViewAction

sealed class SumsViewAction : ViewAction {
    object RerenderAll : SumsViewAction()
}