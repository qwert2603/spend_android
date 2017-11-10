package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.model.schedulers.ModelSchedulersProvider
import com.qwert2603.spenddemo.model.schedulers.SchedulersProviderImpl
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SchedulersModule  {
    private val schedulersProviderImpl = SchedulersProviderImpl()

    @Provides @Singleton fun uiSchedulersProvider(): UiSchedulerProvider = schedulersProviderImpl
    @Provides @Singleton fun modelSchedulersProvider(): ModelSchedulersProvider = schedulersProviderImpl
}