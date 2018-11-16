package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.google.gson.Gson
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.repo.RecordsDraftsRepo
import com.qwert2603.spenddemo.utils.PreferenceUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordsDraftsRepoImpl @Inject constructor(
        appContext: Context
) : RecordsDraftsRepo {

    private val prefs = appContext.getSharedPreferences("drafts.prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    override var spendDraft: RecordDraft? by PreferenceUtils.createPrefsObjectNullable(prefs, "spendDraft", gson)

    override var profitDraft: RecordDraft? by PreferenceUtils.createPrefsObjectNullable(prefs, "profitDraft", gson)
}