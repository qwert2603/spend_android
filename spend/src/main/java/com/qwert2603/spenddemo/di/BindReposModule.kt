package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.model.repo.ChangesRepo
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.model.repo.KindsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo_impl.ChangesRepoImpl
import com.qwert2603.spenddemo.model.repo_impl.DraftRepoImpl
import com.qwert2603.spenddemo.model.repo_impl.KindsRepoImpl
import com.qwert2603.spenddemo.model.repo_impl.RecordsRepoImpl
import dagger.Binds
import dagger.Module

@Module
@Suppress("UNUSED")
interface BindReposModule {
    @Binds fun bind0(recordsRepoImpl: RecordsRepoImpl): RecordsRepo
    @Binds fun bind1(kindsRepoImpl: KindsRepoImpl): KindsRepo
    @Binds fun bind2(changesRepoImpl: ChangesRepoImpl): ChangesRepo
    @Binds fun bind3(draftRepoImpl: DraftRepoImpl): DraftRepo
}