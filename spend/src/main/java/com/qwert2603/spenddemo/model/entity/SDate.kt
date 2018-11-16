package com.qwert2603.spenddemo.model.entity

//todo
inline class SDate(val date: Int) {
    override fun toString() = "${date / (100 * 100)}-${date / 100 % 100}-${date % 100}"
}