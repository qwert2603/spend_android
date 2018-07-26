package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.repo.SpendDraftRepo
import com.qwert2603.spenddemo.utils.PrefsCreatingSpend
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendDraftRepoImpl @Inject constructor(
        appContext: Context
) : SpendDraftRepo {

    companion object {
        private const val DRAFT_FILENAME = "draft.prefs"
    }

    private val prefs = appContext.getSharedPreferences(DRAFT_FILENAME, Context.MODE_PRIVATE)

    private var draft by PrefsCreatingSpend(prefs, "spend_draft", CreatingSpend.EMPTY)

    override fun saveDraft(creatingSpend: CreatingSpend): Completable = Completable.fromAction { draft = creatingSpend }

    override fun getDraft(): Single<CreatingSpend> = Single.just(draft)
}