package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.sql.Query
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import kotlin.reflect.KClass


fun assertEquals(string: String, query: Query) = assertEquals(string, query.query.trim())

fun assertThrowsException(expectedException: KClass<out Exception>, function: () -> Unit) {
    try {
        function()
        fail("Expected call to fail. Unexpectedly passed")
    } catch (e: Exception) {
        if (e.javaClass != expectedException.java) {
            fail("Expected $expectedException but got ${e.javaClass}")
        }
    }
}