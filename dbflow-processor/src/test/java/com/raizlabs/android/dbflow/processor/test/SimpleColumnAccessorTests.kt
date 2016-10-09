package com.raizlabs.android.dbflow.processor.test

import com.raizlabs.android.dbflow.processor.definition.column.GetterSetter
import com.raizlabs.android.dbflow.processor.definition.column.PrivateScopeColumnAccessor
import com.raizlabs.android.dbflow.processor.definition.column.VisibleScopeColumnAccessor
import com.squareup.javapoet.CodeBlock
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */
class VisibleScopeColumnAccessorTest {


    @Test
    fun test_canSetSimpleAccess() {

        val simpleAccess = VisibleScopeColumnAccessor("test")
        assertEquals("test = something", simpleAccess.set(CodeBlock.of("something")).toString())
    }

    @Test
    fun test_canGetSimpleAccess() {
        val simpleAccess = VisibleScopeColumnAccessor("test")
        assertEquals("test", simpleAccess.get().toString())
    }
}

class PrivateScopeColumnAccessorTest {

    @Test
    fun test_canGetPrivateIsAccess() {
        val privateAccess = PrivateScopeColumnAccessor("isTest",
                isBoolean = true,
                useIsForPrivateBooleans = true)

        assertEquals("isTest()", privateAccess.get().toString())
    }

    @Test
    fun test_canGetSimplePrivate() {
        val privateAccess = PrivateScopeColumnAccessor("isTest")
        assertEquals("getIsTest()", privateAccess.get().toString())
    }

    @Test
    fun test_canRetrieveColumnGetterSetter() {
        val privateAccess = PrivateScopeColumnAccessor("isTest",
                object : GetterSetter {
                    override val getterName = "getTest"
                    override val setterName = "isTest"
                })
        assertEquals("getTest()", privateAccess.get().toString())
        assertEquals("isTest(yellow)", privateAccess.set(CodeBlock.of("yellow")).toString())
    }
}