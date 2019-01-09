package com.qwert2603.spenddemo.di.presenters

import com.qwert2603.spenddemo.change_records.ChangeRecordsPresenter
import com.qwert2603.spenddemo.save_record.SaveRecordKey
import com.qwert2603.spenddemo.save_record.SaveRecordPresenter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface ChangeRecordsPresenterCreatorComponent {
    fun createPresenter(): ChangeRecordsPresenter

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun recordsUuids(recordsUuids: List<String>): Builder

        fun build(): ChangeRecordsPresenterCreatorComponent
    }
}