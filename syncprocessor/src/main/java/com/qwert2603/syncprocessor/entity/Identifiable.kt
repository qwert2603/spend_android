package com.qwert2603.syncprocessor.entity

interface Identifiable<out I : Any> {
    val id: I
}