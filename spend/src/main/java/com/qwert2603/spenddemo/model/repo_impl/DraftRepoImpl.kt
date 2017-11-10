package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import android.content.SharedPreferences
import com.qwert2603.spenddemo.draft.DraftInteractor
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
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
                    .putLong(DRAFT_DATE_KEY, creatingRecord.date.time)
                    .apply()
        }

        private fun SharedPreferences.getDraft() = CreatingRecord(
                getString(DRAFT_KIND_KEY, ""),
                getInt(DRAFT_VALUE_KEY, 0),
                Date(getLong(DRAFT_DATE_KEY, System.currentTimeMillis()))
        )
    }

    private val prefs = appContext.getSharedPreferences(DRAFT_FILENAME, Context.MODE_PRIVATE)

    @Volatile private var draft: CreatingRecord = prefs.getDraft()
        set(value) {
            LogUtils.d("DraftRepoImpl ${hashCode()} draft = $value")
            field = value
            prefs.saveDraft(value)
        }

    private val draftChanges: PublishSubject<CreatingRecord> = PublishSubject.create()

    override fun saveDraft(creatingRecord: CreatingRecord): Completable = Completable
            .fromAction { draft = creatingRecord }

    override fun getDraft(): Observable<CreatingRecord> = draftChanges
            .startWith(draft)

    override fun removeDraft(): Completable = Completable.fromAction { draft = DraftInteractor.EmptyCreatingRecord }

    override fun onDateSelected(date: Date) {
        draft = draft.copy(date = date)
        draftChanges.onNext(draft)
    }

    override fun onKindSelected(kind: String) {
        draft = draft.copy(kind = kind)
        draftChanges.onNext(draft)
    }
}