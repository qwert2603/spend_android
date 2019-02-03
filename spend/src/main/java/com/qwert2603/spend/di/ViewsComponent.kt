package com.qwert2603.spend.di

import com.qwert2603.spend.dialogs.*
import com.qwert2603.spend.model.sync_processor.SyncWorker
import com.qwert2603.spend.navigation.MainActivity
import com.qwert2603.spend.sums.SumsFragment
import dagger.Subcomponent

@Subcomponent
interface ViewsComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(chooseLongSumPeriodDialog: ChooseLongSumPeriodDialog)
    fun inject(chooseShortSumPeriodDialog: ChooseShortSumPeriodDialog)
    fun inject(deleteRecordDialogFragment: DeleteRecordDialogFragment)
    fun inject(chooseRecordKindDialogFragment: ChooseRecordKindDialogFragment)
    fun inject(chooseRecordCategoryDialogFragment: ChooseRecordCategoryDialogFragment)
    fun inject(sumsFragment: SumsFragment)
    fun inject(combineRecordsDialogFragment: CombineRecordsDialogFragment)
    fun inject(deleteRecordsListDialogFragment: DeleteRecordsListDialogFragment)
    fun inject(syncWorker: SyncWorker)
    fun inject(notDeletedRecordsHashDialogFragment: NotDeletedRecordsHashDialogFragment)
}