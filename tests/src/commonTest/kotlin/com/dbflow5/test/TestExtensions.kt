package com.dbflow5.test

import com.dbflow5.sql.Query
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.fail

fun String.assertEquals(query: Query) = assertEquals(this, query.query.trim())

fun Query.assertEquals(actual: Query) = assertEquals(query.trim(), actual.query.trim())

inline fun assertThrowsException(expectedException: KClass<out Exception>, function: () -> Unit) {
    try {
        function()
        fail("Expected call to fail. Unexpectedly passed")
    } catch (e: Exception) {
        if (e::class != expectedException) {
            e.printStackTrace()
            fail("Expected $expectedException but got ${e::class}")
        }
    }
}