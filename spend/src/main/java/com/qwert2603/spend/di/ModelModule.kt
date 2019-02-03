package com.qwert2603.spend.di

import android.arch.persistence.room.Room
import android.content.Context
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.env.E
import com.qwert2603.spend.model.local_db.LocalDB
import com.qwert2603.spend.model.local_db.dao.RecordsDao
import com.qwert2603.spend.model.rest.Rest
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun recordsDao(localDB: LocalDB): RecordsDao = localDB.recordsDao()

    @Provides
    @Singleton
    fun rest(): Rest = Retrofit.Builder()
            .baseUrl(E.env.restBaseUrl)
            .client(OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor { LogUtils.d("ok_http", it) }.setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Rest::class.java)

    @Provides
    @Singleton
    @LocalDBExecutor
    fun localDBExecutor(): ExecutorService = Executors.newSingleThreadExecutor()

    @Provides
    @Singleton
    @RemoteDBExecutor
    fun remoteDBExecutor(): ExecutorService = Executors.newSingleThreadExecutor()
}