package com.qwert2603.spend.utils

import com.qwert2603.spend.model.entity.days

object Const {
    const val MIN_YEAR = 1970

    const val MAX_CATEGORY_NAME_LENGTH = 64
    const val MAX_RECORD_KIND_LENGTH = 64

    const val RECORD_TYPE_ID_SPEND = 1L
    const val RECORD_TYPE_ID_PROFIT = 2L

    val RECORD_TYPE_IDS = listOf(
            RECORD_TYPE_ID_SPEND,
            RECORD_TYPE_ID_PROFIT
    )

    const val MAX_RECORDS_TO_SAVE_COUNT = 250
    const val MAX_RECORDS_UPDATES_COUNT = 250

    val CHANGE_RECORD_PAST = 7.days
    const val OLD_RECORDS_UNLOCK_SECONDS = 60
}