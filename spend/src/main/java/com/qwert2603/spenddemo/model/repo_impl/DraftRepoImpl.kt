package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import android.content.SharedPreferences
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.utils.onlyDate
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

        private fun SharedPreferences.saveDraft(creatingRecord: CreatingRecord) {
            edit()
                    .putString(DRAFT_KIND_KEY, creatingRecord.kind)
                    .putInt(DRAFT_VALUE_KEY, creatingRecord.value)
                    .also {
                        if (creatingRecord.date != null) {
                            it.putLong(DRAFT_DATE_KEY, creatingRecord.date.time)
                        } else {
                            it.remove(DRAFT_DATE_KEY)
                        }
                    }
                    .apply()
        }

        private fun SharedPreferences.getDraft() = CreatingRecord(
                getString(DRAFT_KIND_KEY, ""),
                getInt(DRAFT_VALUE_KEY, 0),
                if (contains(DRAFT_DATE_KEY)) {
                    Date(getLong(DRAFT_DATE_KEY, System.currentTimeMillis())).onlyDate()
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

    override fun saveDraft(creatingRecord: CreatingRecord): Completable = Completable.fromAction { prefs.saveDraft(creatingRecord) }

    override fun getDraft(): Single<CreatingRecord> = Single.fromCallable { prefs.getDraft() }

    override fun removeDraft(): Completable = Completable.fromAction { prefs.removeDraft() }
}