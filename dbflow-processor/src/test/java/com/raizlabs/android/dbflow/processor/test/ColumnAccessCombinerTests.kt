package com.raizlabs.android.dbflow.processor.test

import com.raizlabs.android.dbflow.processor.definition.column.*
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description: Tests to ensure we can combine [ColumnAccessor] properly.
 *
 * @author Andrew Grosner (fuzz)
 */
class ContentValuesCombinerTest {

    @Test
    fun test_canCombineSimpleCase() {
        val combiner = ContentValuesCombiner(VisibleScopeColumnAccessor("name"),
                TypeName.get(String::class.java))

        val codeBuilder = CodeBlock.builder()
        combiner.addCode(codeBuilder, "columnName", CodeBlock.of("\$S", "nonNull"))

        assertEquals("values.put(\"columnName\", model.name != null ? model.name : \"nonNull\");",
                codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombineSimplePrimitiveCase() {
        val combiner = ContentValuesCombiner(VisibleScopeColumnAccessor("name"),
                TypeName.get(Boolean::class.java))

        val codeBuilder = CodeBlock.builder()
        combiner.addCode(codeBuilder, "columnName")

        assertEquals("values.put(\"columnName\", model.name);",
                codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombineWrapperCase() {
        val combiner = ContentValuesCombiner(
                PackagePrivateScopeColumnAccessor("name", "com.fuzz.android", "_", "TestType"),
                TypeName.get(String::class.java),
                TypeConverterScopeColumnAccessor("global_converter"),
                TypeName.get(String::class.java))

        val codeBuilder = CodeBlock.builder()
        combiner.addCode(codeBuilder, "columnName", CodeBlock.of("\$S", "nonNull"))

        assertEquals("java.lang.String refname = com.fuzz.android.TestType_Helper.getName(model) != null ? global_converter.getDBValue(com.fuzz.android.TestType_Helper.getName(model)) : null;"
                + "\nvalues.put(\"columnName\", refname != null ? refname : \"nonNull\");",
                codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombinePrivateWrapperCase() {
        val combiner = ContentValuesCombiner(
                PrivateScopeColumnAccessor("name"),
                TypeName.get(String::class.java),
                TypeConverterScopeColumnAccessor("global_converter"),
                TypeName.get(String::class.java))

        val codeBuilder = CodeBlock.builder()
        combiner.addCode(codeBuilder, "columnName", CodeBlock.of("\$S", "nonNull"))

        assertEquals("java.lang.String refname = model.getName() != null ? global_converter.getDBValue(model.getName()) : null;"
                + "\nvalues.put(\"columnName\", refname != null ? refname : \"nonNull\");",
                codeBuilder.build().toString().trim())
    }


}

class SqliteStatementAccessCombinerTest {

    @Test
    fun test_canCombineSimpleCase() {
        val combiner = SqliteStatementAccessCombiner(VisibleScopeColumnAccessor("name"),
                TypeName.get(String::class.java))

        val codeBuilder = CodeBlock.builder()
        combiner.addCode(codeBuilder, "0 + start", CodeBlock.of("\$S", "nonNull"))

        assertEquals("statement.bindString(0 + start, model.name != null ? model.name : \"nonNull\");",
                codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombineSimplePrimitiveCase() {
        val combiner = SqliteStatementAccessCombiner(VisibleScopeColumnAccessor("name"),
                TypeName.get(Boolean::class.java),
                BooleanColumnAccessor(),
                TypeName.get(Boolean::class.java))

        val codeBuilder = CodeBlock.builder()
        combiner.addCode(codeBuilder, "0 + start")

        assertEquals("statement.bindLong(0 + start, model.name ? 1 : 0);",
                codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombineWrapperCase() {
        val combiner = SqliteStatementAccessCombiner(
                PackagePrivateScopeColumnAccessor("name", "com.fuzz.android", "_", "TestType"),
                TypeName.get(String::class.java),
                TypeConverterScopeColumnAccessor("global_converter"),
                TypeName.get(String::class.java))

        val codeBuilder = CodeBlock.builder()
        combiner.addCode(codeBuilder, "0 + start", CodeBlock.of("\$S", "nonNull"))

        assertEquals("java.lang.String refname = com.fuzz.android.TestType_Helper.getName(model) != null ? global_converter.getDBValue(com.fuzz.android.TestType_Helper.getName(model)) : null;"
                + "\nstatement.bindString(0 + start, refname != null ? refname : \"nonNull\");",
                codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombinePrivateWrapperCase() {
        val combiner = SqliteStatementAccessCombiner(
                PrivateScopeColumnAccessor("name"),
                TypeName.get(String::class.java),
                TypeConverterScopeColumnAccessor("global_converter"),
                TypeName.get(String::class.java))

        val codeBuilder = CodeBlock.builder()
        combiner.addCode(codeBuilder, "1 + start", CodeBlock.of("\$S", "nonNull"))

        assertEquals("java.lang.String refname = model.getName() != null ? global_converter.getDBValue(model.getName()) : null;"
                + "\nstatement.bindString(1 + start, refname != null ? refname : \"nonNull\");",
                codeBuilder.build().toString().trim())
    }

}