package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.dialogs.ChooseLongSumPeriodDialog
import com.qwert2603.spenddemo.dialogs.ChooseRecordKindDialogFragment
import com.qwert2603.spenddemo.dialogs.ChooseShortSumPeriodDialog
import com.qwert2603.spenddemo.dialogs.DeleteRecordDialogFragment
import com.qwert2603.spenddemo.navigation.MainActivity
import dagger.Subcomponent

@Subcomponent
interface ViewsComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(chooseLongSumPeriodDialog: ChooseLongSumPeriodDialog)
    fun inject(chooseShortSumPeriodDialog: ChooseShortSumPeriodDialog)
    fun inject(deleteRecordDialogFragment: DeleteRecordDialogFragment)
    fun inject(chooseRecordKindDialogFragment: ChooseRecordKindDialogFragment)
}