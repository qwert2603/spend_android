package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.SDate
import java.io.Serializable

// don't use objects for RecordsListKey.Now because of serialization.
sealed class RecordsListKey : Serializable {
    data class Now(@Transient private val ignored: Unit = Unit) : RecordsListKey()
    data class Date(val date: SDate) : RecordsListKey()
}