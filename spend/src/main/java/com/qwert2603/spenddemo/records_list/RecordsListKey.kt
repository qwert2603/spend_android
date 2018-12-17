package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.SDate
import java.io.Serializable

sealed class RecordsListKey : Serializable {
    object Now : RecordsListKey()
    data class Date(val date: SDate) : RecordsListKey()
}