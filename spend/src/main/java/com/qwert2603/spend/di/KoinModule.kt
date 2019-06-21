package com.qwert2603.spend.di

import androidx.room.Room
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.about.AboutInteractor
import com.qwert2603.spend.about.AboutPresenter
import com.qwert2603.spend.change_records.ChangeRecordsInteractor
import com.qwert2603.spend.change_records.ChangeRecordsPresenter
import com.qwert2603.spend.env.E
import com.qwert2603.spend.model.local_db.LocalDB
import com.qwert2603.spend.model.repo.RecordAggregationsRepo
import com.qwert2603.spend.model.repo.RecordsDraftsRepo
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.model.repo.UserSettingsRepo
import com.qwert2603.spend.model.repo_impl.RecordAggregationsRepoImpl
import com.qwert2603.spend.model.repo_impl.RecordsDraftsRepoImpl
import com.qwert2603.spend.model.repo_impl.RecordsRepoImpl
import com.qwert2603.spend.model.repo_impl.UserSettingsRepoImpl
import com.qwert2603.spend.model.rest.ApiHelper
import com.qwert2603.spend.model.rest.Rest
import com.qwert2603.spend.model.schedulers.SchedulersProviderImpl
import com.qwert2603.spend.model.sync_processor.IsShowingToUserHolder
import com.qwert2603.spend.model.sync_processor.SyncProcessor
import com.qwert2603.spend.records_list.RecordsListInteractor
import com.qwert2603.spend.records_list.RecordsListPresenter
import com.qwert2603.spend.save_record.SaveRecordInteractor
import com.qwert2603.spend.save_record.SaveRecordKey
import com.qwert2603.spend.save_record.SaveRecordPresenter
import com.qwert2603.spend.sums.SumsInteractor
import com.qwert2603.spend.sums.SumsPresenter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import java.util.concurrent.Executors
import com.qwert2603.spend.records_list_view.RecordsListInteractor as RecordsListViewInteractor
import com.qwert2603.spend.records_list_view.RecordsListPresenter as RecordsListViewPresenter

val navigationModule = module {
    val cicerone: Cicerone<Router> = Cicerone.create()

    single<NavigatorHolder> { cicerone.navigatorHolder }
    single<Router> { cicerone.router }
}

val modelModule = module {
    single(named("localDbExecutor")) { Executors.newSingleThreadExecutor() }
    single(named("remoteDbExecutor")) { Executors.newSingleThreadExecutor() }

    single<OkHttpClient> {
        OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor { LogUtils.d("ok_http", it) }.setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
    }

    single<Rest> {
        Retrofit.Builder()
                .baseUrl(E.env.restBaseUrl)
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Rest::class.java)
    }

    single {
        Room
                .databaseBuilder(get(), LocalDB::class.java, "local_db")
                .build()
    }

    single { get<LocalDB>().recordsDao() }

    single { ApiHelper(get()) }

    single {
        SyncProcessor(
                appContext = get(),
                remoteDBExecutor = get(named("localDbExecutor")),
                localDBExecutor = get(named("remoteDbExecutor")),
                apiHelper = get(),
                recordsDao = get()
        )
    }

    single { IsShowingToUserHolder() }
}

val schedulersModule = module {
    val schedulersProviderImpl = SchedulersProviderImpl()

    single<ModelSchedulersProvider> { schedulersProviderImpl }
    single<UiSchedulerProvider> { schedulersProviderImpl }
}

val repoModule = module {
    single<UserSettingsRepo> { UserSettingsRepoImpl(get()) }
    single<RecordsRepo> { RecordsRepoImpl(get(), get(), get(), get(), get()) }
    single<RecordAggregationsRepo> { RecordAggregationsRepoImpl(get(), get()) }
    single<RecordsDraftsRepo> { RecordsDraftsRepoImpl(get()) }
}

val interactorsModule = module {
    factory { AboutInteractor(get()) }
    factory { RecordsListInteractor(get(), get(), get()) }
    factory { RecordsListViewInteractor(get()) }
    factory { SumsInteractor(get(), get()) }
    factory { ChangeRecordsInteractor(get()) }
    factory { SaveRecordInteractor(get(), get(), get()) }
}

val presentersModule = module {
    factory { AboutPresenter(get(), get()) }
    factory { SumsPresenter(get(), get()) }
    factory { RecordsListPresenter(get(), get()) }
    factory { (recordsUuids: List<String>) -> RecordsListViewPresenter(recordsUuids, get(), get()) }
    factory { (recordsUuids: List<String>) -> ChangeRecordsPresenter(recordsUuids, get(), get()) }
    factory { (saveRecordKey: SaveRecordKey) -> SaveRecordPresenter(saveRecordKey, get(), get()) }
}