package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.Days
import com.qwert2603.spenddemo.model.entity.Minutes
import com.qwert2603.spenddemo.model.entity.ShowInfo
import com.qwert2603.spenddemo.model.entity.SumsShowInfo
import com.qwert2603.spenddemo.utils.ObservableField

interface UserSettingsRepo {
    val longSumPeriod: ObservableField<Days>
    val shortSumPeriod: ObservableField<Minutes>
    val showInfo: ObservableField<ShowInfo>
    val sumsShowInfo: ObservableField<SumsShowInfo>
}