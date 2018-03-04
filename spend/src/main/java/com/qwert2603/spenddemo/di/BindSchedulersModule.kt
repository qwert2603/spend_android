package com.qwert2603.spenddemo.di

import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.schedulers.SchedulersProviderImpl
import dagger.Binds
import dagger.Module

@Module
@Suppress("UNUSED")
interface BindSchedulersModule  {
    @Binds fun bind0(schedulersProviderImpl: SchedulersProviderImpl): ModelSchedulersProvider
    @Binds fun bind1(schedulersProviderImpl: SchedulersProviderImpl): UiSchedulerProvider
}