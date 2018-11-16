package com.qwert2603.spenddemo.model.sync_processor

interface IdentifiableString {
    val uuid: String

    companion object {
        const val NO_UUID = ""
    }
}