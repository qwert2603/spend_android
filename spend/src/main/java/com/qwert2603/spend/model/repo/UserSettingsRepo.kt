package com.qwert2603.spend.model.repo

import com.qwert2603.spend.model.entity.Days
import com.qwert2603.spend.model.entity.Minutes
import com.qwert2603.spend.model.entity.ShowInfo
import com.qwert2603.spend.model.entity.SumsShowInfo
import com.qwert2603.spend.utils.ObservableField

interface UserSettingsRepo {
    val longSumPeriod: ObservableField<Days>
    val shortSumPeriod: ObservableField<Minutes>
    val showInfo: ObservableField<ShowInfo>
    val sumsShowInfo: ObservableField<SumsShowInfo>
}