package com.qwert2603.spend.records_list_view

import com.qwert2603.andrlib.base.mvi.ViewAction

sealed class RecordsListViewAction : ViewAction {
    object RerenderAll : RecordsListViewAction()
}