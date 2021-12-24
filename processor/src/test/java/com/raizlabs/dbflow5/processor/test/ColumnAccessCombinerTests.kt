package com.dbflow5.processor.test

import com.dbflow5.processor.definition.behavior.CursorHandlingBehavior
import com.dbflow5.processor.definition.column.BooleanColumnAccessor
import com.dbflow5.processor.definition.column.Combiner
import com.dbflow5.processor.definition.column.ContentValuesCombiner
import com.dbflow5.processor.definition.column.LoadFromCursorAccessCombiner
import com.dbflow5.processor.definition.column.PackagePrivateScopeColumnAccessor
import com.dbflow5.processor.definition.column.PrimaryReferenceAccessCombiner
import com.dbflow5.processor.definition.column.PrivateScopeColumnAccessor
import com.dbflow5.processor.definition.column.SqliteStatementAccessCombiner
import com.dbflow5.processor.definition.column.TypeConverterScopeColumnAccessor
import com.dbflow5.processor.definition.column.VisibleScopeColumnAccessor
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.TypeName
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Description: Tests to ensure we can combine [ColumnAccessor] properly.
 *
 * @author Andrew Grosner (fuzz)
 */
class ContentValuesCombinerTest {

    @Test
    fun test_canCombineSimpleCase() {
        val combiner = ContentValuesCombiner(Combiner(VisibleScopeColumnAccessor("name"),
            TypeName.get(String::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("columnName", CodeBlock.of("\$S", "nonNull"), -1)
        }

        assertEquals("values.put(\"`columnName`\", model.name != null ? model.name : \"nonNull\");",
            codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombineSimplePrimitiveCase() {
        val combiner = ContentValuesCombiner(Combiner(VisibleScopeColumnAccessor("name"),
            TypeName.get(Boolean::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("columnName", index = -1)
        }

        assertEquals("values.put(\"`columnName`\", model.name);",
            codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombineWrapperCase() {
        val combiner = ContentValuesCombiner(
            Combiner(PackagePrivateScopeColumnAccessor("name", "com.fuzz.android", "TestType"),
                TypeName.get(String::class.java),
                TypeConverterScopeColumnAccessor("global_converter"),
                TypeName.get(String::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("columnName", CodeBlock.of("\$S", "nonNull"), -1)
        }

        assertEquals("java.lang.String refname = com.fuzz.android.TestType_Helper.getName(model) != null ? global_converter.getDBValue(com.fuzz.android.TestType_Helper.getName(model)) : null;"
            + "\nvalues.put(\"`columnName`\", refname != null ? refname : \"nonNull\");",
            codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombinePrivateWrapperCase() {
        val combiner = ContentValuesCombiner(
            Combiner(PrivateScopeColumnAccessor("name"),
                TypeName.get(String::class.java),
                TypeConverterScopeColumnAccessor("global_converter"),
                TypeName.get(String::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("columnName", CodeBlock.of("\$S", "nonNull"), -1)
        }

        assertEquals("java.lang.String refname = model.getName() != null ? global_converter.getDBValue(model.getName()) : null;"
            + "\nvalues.put(\"`columnName`\", refname != null ? refname : \"nonNull\");",
            codeBuilder.build().toString().trim())
    }


}

class SqliteStatementAccessCombinerTest {

    @Test
    fun test_canCombineSimpleCase() {
        val combiner = SqliteStatementAccessCombiner(
            Combiner(VisibleScopeColumnAccessor("name"), TypeName.get(String::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("", CodeBlock.of("\$S", "nonNull"), 0)
        }

        assertEquals("if (model.name != null) {" +
            "\n  statement.bindString(0, model.name);" +
            "\n} else {" +
            "\n  statement.bindString(0, \"nonNull\");" +
            "\n}", codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombineSimplePrimitiveCase() {
        val combiner = SqliteStatementAccessCombiner(
            Combiner(VisibleScopeColumnAccessor("name"),
                TypeName.get(Boolean::class.java),
                BooleanColumnAccessor(),
                TypeName.get(Boolean::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("", index = 0)
        }

        assertEquals("statement.bindLong(0, model.name ? 1 : 0);",
            codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombineWrapperCase() {
        val combiner = SqliteStatementAccessCombiner(
            Combiner(PackagePrivateScopeColumnAccessor("name", "com.fuzz.android", "TestType"),
                TypeName.get(String::class.java),
                TypeConverterScopeColumnAccessor("global_converter"),
                TypeName.get(String::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("", CodeBlock.of("\$S", "nonNull"), 1)
        }

        assertEquals("java.lang.String refname = com.fuzz.android.TestType_Helper.getName(model) != null ? global_converter.getDBValue(com.fuzz.android.TestType_Helper.getName(model)) : null;" +
            "\nif (refname != null) {" +
            "\n  statement.bindString(1, refname);" +
            "\n} else {" +
            "\n  statement.bindString(1, \"nonNull\");" +
            "\n}", codeBuilder.build().toString().trim())
    }

    @Test
    fun test_canCombinePrivateWrapperCase() {
        val combiner = SqliteStatementAccessCombiner(
            Combiner(PrivateScopeColumnAccessor("name"),
                TypeName.get(String::class.java),
                TypeConverterScopeColumnAccessor("global_converter"),
                TypeName.get(String::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("", CodeBlock.of("\$S", "nonNull"), 1)
        }
        assertEquals("java.lang.String refname = model.getName() != null ? global_converter.getDBValue(model.getName()) : null;" +
            "\nif (refname != null) {" +
            "\n  statement.bindString(1, refname);" +
            "\n} else {" +
            "\n  statement.bindString(1, \"nonNull\");" +
            "\n}", codeBuilder.build().toString().trim())
    }

}

class LoadFromCursorAccessCombinerTest {

    @Test
    fun test_simpleCase() {
        val combiner = LoadFromCursorAccessCombiner(
            Combiner(VisibleScopeColumnAccessor("name"), TypeName.get(String::class.java)), false, NameAllocator(),
            CursorHandlingBehavior())
        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("columnName", CodeBlock.of("\$S", "nonNull"))
        }

        assertEquals("model.name = cursor.getStringOrDefault(\"columnName\");", codeBuilder.build().toString().trim())
    }

    @Test
    fun test_wrapperCase() {
        val combiner = LoadFromCursorAccessCombiner(
            Combiner(VisibleScopeColumnAccessor("name"),
                TypeName.get(Date::class.java),
                wrapperLevelAccessor = TypeConverterScopeColumnAccessor("global_converter"),
                wrapperFieldTypeName = TypeName.get(String::class.java)), false, NameAllocator(),
            CursorHandlingBehavior())
        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("columnName", CodeBlock.of("\$S", "nonNull"))
        }

        assertEquals("int index_columnName = cursor.getColumnIndex(\"columnName\");" +
            "\nif (index_columnName != -1 && !cursor.isNull(index_columnName)) {" +
            "\n  model.name = global_converter.getModelValue(cursor.getString(index_columnName));" +
            "\n} else {" +
            "\n  model.name = global_converter.getModelValue(\"nonNull\");" +
            "\n}", codeBuilder.build().toString().trim())
    }
}

class PrimaryReferenceAccessCombinerTest {

    @Test
    fun test_simpleCase() {
        val combiner = PrimaryReferenceAccessCombiner(
            Combiner(VisibleScopeColumnAccessor("id"), TypeName.get(Long::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("id")
        }

        assertEquals("clause.and(id.eq(model.id));", codeBuilder.build().toString().trim())
    }

    @Test
    fun test_typeConverterCase() {
        val combiner = PrimaryReferenceAccessCombiner(
            Combiner(VisibleScopeColumnAccessor("id"),
                TypeName.get(Long::class.java).box(),
                TypeConverterScopeColumnAccessor("global_converter"),
                TypeName.get(Date::class.java)))

        val codeBuilder = CodeBlock.builder()
        combiner.apply {
            codeBuilder.addCode("id")
        }

        assertEquals("java.util.Date refid = model.id != null ? global_converter.getDBValue(model.id) : null;\n" +
            "clause.and(id.dataProperty.eq(refid));",
            codeBuilder.build().toString().trim())
    }
}