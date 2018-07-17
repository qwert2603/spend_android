package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import android.content.SharedPreferences
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.repo.SpendDraftRepo
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendDraftRepoImpl @Inject constructor(
        appContext: Context
) : SpendDraftRepo {

    companion object {
        private const val DRAFT_FILENAME = "draft.prefs"

        private const val DRAFT_DATE_KEY = "DRAFT_DATE_KEY"
        private const val DRAFT_KIND_KEY = "DRAFT_KIND_KEY"
        private const val DRAFT_VALUE_KEY = "DRAFT_VALUE_KEY"

        private fun SharedPreferences.saveDraft(creatingSpend: CreatingSpend) {
            edit()
                    .putString(DRAFT_KIND_KEY, creatingSpend.kind)
                    .putInt(DRAFT_VALUE_KEY, creatingSpend.value)
                    .also {
                        if (creatingSpend.date != null) {
                            it.putLong(DRAFT_DATE_KEY, creatingSpend.date.time)
                        } else {
                            it.remove(DRAFT_DATE_KEY)
                        }
                    }
                    .apply()
        }

        private fun SharedPreferences.getDraft() = CreatingSpend(
                getString(DRAFT_KIND_KEY, ""),
                getInt(DRAFT_VALUE_KEY, 0),
                if (contains(DRAFT_DATE_KEY)) {
                    Date(getLong(DRAFT_DATE_KEY, 0))
                } else {
                    null
                }
        )

        private fun SharedPreferences.removeDraft() {
            edit()
                    .remove(DRAFT_KIND_KEY)
                    .remove(DRAFT_VALUE_KEY)
                    .remove(DRAFT_DATE_KEY)
                    .apply()
        }
    }

    private val prefs = appContext.getSharedPreferences(DRAFT_FILENAME, Context.MODE_PRIVATE)

    override fun saveDraft(creatingSpend: CreatingSpend): Completable = Completable.fromAction { prefs.saveDraft(creatingSpend) }

    override fun getDraft(): Single<CreatingSpend> = Single.fromCallable { prefs.getDraft() }

    override fun removeDraft(): Completable = Completable.fromAction { prefs.removeDraft() }
}