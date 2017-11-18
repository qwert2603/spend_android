package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import android.content.SharedPreferences
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.repo.DraftRepo
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DraftRepoImpl @Inject constructor(
        appContext: Context
) : DraftRepo {

    companion object {
        private const val DRAFT_FILENAME = "draft.prefs"

        private const val DRAFT_DATE_KEY = "DRAFT_DATE_KEY"
        private const val DRAFT_KIND_KEY = "DRAFT_KIND_KEY"
        private const val DRAFT_VALUE_KEY = "DRAFT_VALUE_KEY"
        private const val DRAFT_DATE_SET_KEY = "DRAFT_DATE_SET_KEY"

        private fun SharedPreferences.saveDraft(creatingRecord: CreatingRecord) {
            edit()
                    .putString(DRAFT_KIND_KEY, creatingRecord.kind)
                    .putInt(DRAFT_VALUE_KEY, creatingRecord.value)
                    .putLong(DRAFT_DATE_KEY, creatingRecord.date.time)
                    .putBoolean(DRAFT_DATE_SET_KEY, creatingRecord.dateSet)
                    .apply()
        }

        private fun SharedPreferences.getDraft() = CreatingRecord(
                getString(DRAFT_KIND_KEY, ""),
                getInt(DRAFT_VALUE_KEY, 0),
                Date(getLong(DRAFT_DATE_KEY, System.currentTimeMillis())),
                getBoolean(DRAFT_DATE_SET_KEY, false)
        )
    }

    private val prefs = appContext.getSharedPreferences(DRAFT_FILENAME, Context.MODE_PRIVATE)

    override fun saveDraft(creatingRecord: CreatingRecord): Completable = Completable.fromAction { prefs.saveDraft(creatingRecord) }

    override fun getDraft(): Single<CreatingRecord> = Single.fromCallable { prefs.getDraft() }
}