package com.qwert2603.spenddemo.model.rest.entity

import com.qwert2603.spenddemo.model.sync_processor.IdentifiableString

data class RecordServer(
        override val uuid: String,
        val recordTypeId: Long,
        val date: Int,
        val time: Int?,
        val kind: String,
        val value: Int
) : IdentifiableString