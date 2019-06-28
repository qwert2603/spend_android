package com.qwert2603.spend.records_list

import androidx.annotation.Keep
import com.qwert2603.spend.model.entity.SDate
import java.io.Serializable

// don't use objects for RecordsListKey.Now because of serialization.
@Keep
sealed class RecordsListKey : Serializable {
    data class Now(@Transient private val ignored: Unit? = null) : RecordsListKey()
    data class Date(val date: SDate) : RecordsListKey()
}