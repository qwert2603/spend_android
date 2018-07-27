package com.qwert2603.spenddemo.di

import android.arch.persistence.room.Room
import android.content.Context
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.remote_db.RemoteDB
import com.qwert2603.spenddemo.model.remote_db.RemoteDBImpl
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import dagger.Module
import dagger.Provides
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
class ModelModule {
    @Provides
    @Singleton
    fun localDB(appContext: Context) = Room
            .databaseBuilder(appContext, LocalDB::class.java, "local_db")
            .build()

    @Provides
    @Singleton
    fun remoteDB(userSettingsRepo: UserSettingsRepo): RemoteDB = RemoteDBImpl(userSettingsRepo.serverInfoChanges())

    @Provides
    @Singleton
    @LocalDBExecutor
    fun localDBExecutor(): ExecutorService = Executors.newSingleThreadExecutor()

    @Provides
    @Singleton
    @RemoteDBExecutor
    fun remoteDBExecutor(): ExecutorService = Executors.newSingleThreadExecutor()
}