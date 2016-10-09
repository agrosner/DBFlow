package com.raizlabs.android.dbflow.processor.test

import com.raizlabs.android.dbflow.processor.definition.column.*
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import org.junit.Assert.assertEquals
import org.junit.Before
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

class PackagePrivateScopeColumnAccessorTest {

    @Test
    fun test_canSetupClass() {
        val access = PackagePrivateScopeColumnAccessor("test", "com.fuzz.android", "_", "TestClass")
        assertEquals("com.fuzz.android.TestClass_Helper", access.helperClassName.toString())
        assertEquals("com.fuzz.android.TestClass_Helper", access.internalHelperClassName.toString())
    }

    @Test
    fun test_canGetVariable() {
        val access = PackagePrivateScopeColumnAccessor("test", "com.fuzz.android", "_", "TestClass")
        assertEquals("com.fuzz.android.TestClass_Helper.getTest(model)", access.get(CodeBlock.of("model")).toString())
    }

    @Test
    fun test_canSetVariable() {
        val access = PackagePrivateScopeColumnAccessor("test", "com.fuzz.android", "_", "TestClass")
        assertEquals("com.fuzz.android.TestClass_Helper.setTest(model, \"name\")",
                access.set(CodeBlock.of("\$S", "name"), CodeBlock.of("model")).toString())
    }

}

class TypeConverterScopeColumnAccessorTest {

    lateinit var access: TypeConverterScopeColumnAccessor

    @Before
    fun setup_converter() {
        access = TypeConverterScopeColumnAccessor("global_typeConverterDateConverter")
    }

    @Test
    fun test_canGetConversion() {
        assertEquals("global_typeConverterDateConverter.getModelValue(cursor.getString(\"name\"))",
                access.set(CodeBlock.of("cursor.getString(\"name\")")).toString())
    }

    @Test
    fun test_canSetConversion() {
        assertEquals("global_typeConverterDateConverter.getDBValue(model.name)",
                access.get(CodeBlock.of("model.name")).toString())
    }
}

class EnumColumnAccessorTest {

    enum class TestEnum {
        NAME
    }

    @Test
    fun test_canGetEnum() {
        val access = EnumColumnAccessor(TypeName.get(TestEnum::class.java))
        assertEquals("candy.name()", access.get(CodeBlock.of("candy")).toString())
    }

    @Test
    fun test_canSetEnum() {
        val access = EnumColumnAccessor(TypeName.get(TestEnum::class.java))
        assertEquals("TestEnum.valueOf(model.test)", access.set(CodeBlock.of("model.test")).toString())
    }
}