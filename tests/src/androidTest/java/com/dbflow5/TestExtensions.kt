package com.dbflow5

import com.dbflow5.sql.Query
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import kotlin.reflect.KClass


fun String.assertEquals(query: Query) = assertEquals(this, query.query.trim())

fun Query.assertEquals(actual: Query) = assertEquals(query.trim(), actual.query.trim())

inline fun assertThrowsException(expectedException: KClass<out Exception>, function: () -> Unit) {
    try {
        function()
        fail("Expected call to fail. Unexpectedly passed")
    } catch (e: Exception) {
        if (e.javaClass != expectedException.java) {
            e.printStackTrace()
            fail("Expected $expectedException but got ${e.javaClass}")
        }
    }
}