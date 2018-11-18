package com.qwert2603.spenddemo.utils

object Const {
    const val MIN_YEAR = 1970

    const val MAX_KIND_LENGTH = 64

    const val RECORD_TYPE_ID_SPEND = 1L
    const val RECORD_TYPE_ID_PROFIT = 2L

    val RECORD_TYPE_IDS = listOf(
            RECORD_TYPE_ID_SPEND,
            RECORD_TYPE_ID_PROFIT
    )

    const val CHANGE_KIND_UPSERT = 1
    const val CHANGE_KIND_DELETE = 2

    const val MAX_RECORDS_TO_SAVE_COUNT = 250
    const val MAX_RECORDS_UPDATES_COUNT = 250
}