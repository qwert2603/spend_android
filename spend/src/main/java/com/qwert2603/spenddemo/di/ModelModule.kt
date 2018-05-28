package com.qwert2603.spenddemo.di

import android.arch.persistence.room.Room
import android.content.Context
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.remote_db.RemoteDB
import com.qwert2603.spenddemo.model.remote_db.RemoteDBImpl
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
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
    fun remoteDB(): RemoteDB = RemoteDBImpl(
            "jdbc:postgresql://192.168.1.26:5432/spend",
            "postgres",
            "1234"
    )

    @Provides
    @Singleton
    fun dbExecutor(): Executor = Executors.newSingleThreadExecutor()
}