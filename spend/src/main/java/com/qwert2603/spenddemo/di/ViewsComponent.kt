package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.dialogs.ChooseKindDialogFragment
import com.qwert2603.spenddemo.dialogs.ChooseProfitKindDialogFragment
import com.qwert2603.spenddemo.dialogs.DeleteProfitDialogFragment
import com.qwert2603.spenddemo.dialogs.DeleteRecordDialogFragment
import com.qwert2603.spenddemo.navigation.MainActivity
import com.qwert2603.spenddemo.records_list.RecordsListFragment
import dagger.Subcomponent

@Subcomponent
interface ViewsComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(recordsListFragment: RecordsListFragment)
    fun inject(chooseKindDialogFragment: ChooseKindDialogFragment)
    fun inject(deleteRecordDialogFragment: DeleteRecordDialogFragment)
    fun inject(deleteProfitDialogFragment: DeleteProfitDialogFragment)
    fun inject(chooseProfitKindDialogFragment: ChooseProfitKindDialogFragment)
}