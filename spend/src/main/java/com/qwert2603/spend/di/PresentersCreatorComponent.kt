package com.qwert2603.spend.di

import com.qwert2603.spend.di.presenters.*
import dagger.Subcomponent

@Subcomponent
interface PresentersCreatorComponent {
    fun saveRecordPresenterCreatorComponent(): SaveRecordPresenterCreatorComponent.Builder
    fun recordsListPresenterCreatorComponent(): RecordsListPresenterCreatorComponent.Builder
    fun aboutPresenterCreatorComponent(): AboutPresenterCreatorComponent.Builder
    fun sumsPresenterCreatorComponent(): SumsPresenterCreatorComponent.Builder
    fun changeRecordsPresenterCreatorComponent(): ChangeRecordsPresenterCreatorComponent.Builder
    fun recordsListViewPresenterCreatorComponent(): RecordsListViewPresenterCreatorComponent.Builder
}