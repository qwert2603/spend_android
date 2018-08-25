package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.navigation.MainActivity
import com.qwert2603.spenddemo.records_list_mvvm.ViewModelFactory
import dagger.Subcomponent

@Subcomponent
interface ViewsComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(chooseSpendKindDialogFragment: ChooseSpendKindDialogFragment)
    fun inject(chooseProfitKindDialogFragment: ChooseProfitKindDialogFragment)
    fun inject(viewModelFactory: ViewModelFactory)
    fun inject(serverInfoDialog: ServerInfoDialog)
    fun inject(chooseLongSumPeriodDialog: ChooseLongSumPeriodDialog)
    fun inject(chooseShortSumPeriodDialog: ChooseShortSumPeriodDialog)
    fun inject(lastFullSyncDialog: LastFullSyncDialog)
    fun inject(deleteSpendDialogFragment: DeleteSpendDialogFragment)
    fun inject(deleteProfitDialogFragment: DeleteProfitDialogFragment)
}