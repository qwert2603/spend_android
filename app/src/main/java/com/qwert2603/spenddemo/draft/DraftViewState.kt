package com.qwert2603.spenddemo.draft

import java.util.*

data class DraftViewState(
        val kind: String,
        val value: Int,
        val date: Date,
        val createEnable: Boolean
)