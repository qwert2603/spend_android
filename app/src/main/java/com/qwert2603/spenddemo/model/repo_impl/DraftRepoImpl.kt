package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DraftRepoImpl @Inject constructor() : DraftRepo {
    @Volatile private var draft: CreatingRecord? = null
        set(value) {
            LogUtils.d("DraftRepoImpl ${hashCode()} draft = $value")
            field = value
        }

    override fun saveDraft(creatingRecord: CreatingRecord): Completable = Completable.fromAction { draft = creatingRecord }

    override fun getDraft(): Single<CreatingRecord> = draft?.let { Single.just(it) } ?: Single.error(RuntimeException())

    override fun removeDraft(): Completable = Completable.fromAction { draft = null }
}