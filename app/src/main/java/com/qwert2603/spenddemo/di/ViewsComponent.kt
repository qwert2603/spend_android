package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.dialogs.ChooseKindDialogFragment
import com.qwert2603.spenddemo.navigation.MainActivity
import com.qwert2603.spenddemo.records_list.RecordsListFragment
import dagger.Subcomponent

@Subcomponent
interface ViewsComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(recordsListFragment: RecordsListFragment)
    fun inject(chooseKindDialogFragment: ChooseKindDialogFragment)
}