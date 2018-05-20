package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.dialogs.ChooseProfitKindDialogFragment
import com.qwert2603.spenddemo.dialogs.ChooseSpendKindDialogFragment
import com.qwert2603.spenddemo.dialogs.DeleteProfitDialogFragment
import com.qwert2603.spenddemo.dialogs.DeleteSpendDialogFragment
import com.qwert2603.spenddemo.navigation.MainActivity
import com.qwert2603.spenddemo.records_list.RecordsListFragment
import com.qwert2603.spenddemo.records_list_mvvm.ViewModelFactory
import dagger.Subcomponent

@Subcomponent
interface ViewsComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(recordsListFragment: RecordsListFragment)
    fun inject(chooseSpendKindDialogFragment: ChooseSpendKindDialogFragment)
    fun inject(deleteSpendDialogFragment: DeleteSpendDialogFragment)
    fun inject(deleteProfitDialogFragment: DeleteProfitDialogFragment)
    fun inject(chooseProfitKindDialogFragment: ChooseProfitKindDialogFragment)
    fun inject(viewModelFactory: ViewModelFactory)
}