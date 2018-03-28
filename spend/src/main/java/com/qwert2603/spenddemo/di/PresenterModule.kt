package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.di.qualifiers.ShowChangeKinds
import com.qwert2603.spenddemo.di.qualifiers.ShowIds
import com.qwert2603.spenddemo.model.ServerType
import dagger.Module
import dagger.Provides

@Module
class PresenterModule {
    @Provides
    @ShowChangeKinds
    fun showChangeKinds(): Boolean = when (BuildConfig.SERVER_TYPE) {
        ServerType.NO_SERVER -> false
        ServerType.SERVER_TEST -> true
        ServerType.SERVER_PROD -> true
    }

    @Provides
    @ShowIds
    fun showIds(): Boolean = false
}

