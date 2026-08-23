package com.alexander.pacenote.domain.util

fun interface IdGenerator {
    fun generate(): String
}

fun interface TimeProvider {
    fun nowEpochMillis(): Long
}
