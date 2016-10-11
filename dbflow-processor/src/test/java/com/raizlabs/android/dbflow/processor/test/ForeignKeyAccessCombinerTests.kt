package com.raizlabs.android.dbflow.processor.test

import com.raizlabs.android.dbflow.processor.definition.column.*
import com.raizlabs.android.dbflow.processor.definition.column.PrimaryReferenceAccessCombiner
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */


class ForeignKeyAccessCombinerTest {

    @Test
    fun test_canCombineSimpleCase() {
        val foreignKeyAccessCombiner = ForeignKeyAccessCombiner(VisibleScopeColumnAccessor("name"))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("test",
                ContentValuesCombiner(VisibleScopeColumnAccessor("test"), TypeName.get(String::class.java)))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("test2",
                ContentValuesCombiner(PrivateScopeColumnAccessor("test2"), TypeName.get(Int::class.java)))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(4))

        assertEquals("if (model.name != null) {" +
                "\n  values.put(\"test\", model.name.test);" +
                "\n  values.put(\"test2\", model.name.getTest2());" +
                "\n} else {" +
                "\n  values.putNull(\"test\");" +
                "\n  values.putNull(\"test2\");" +
                "\n}",
                builder.build().toString().trim())
    }

    @Test
    fun test_canCombineSimplePrivateCase() {
        val foreignKeyAccessCombiner = ForeignKeyAccessCombiner(PrivateScopeColumnAccessor("name"))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("start",
                SqliteStatementAccessCombiner(VisibleScopeColumnAccessor("test"), TypeName.get(String::class.java)))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(4))

        assertEquals("if (model.getName() != null) {" +
                "\n  statement.bindString(4 + start, model.getName().test);" +
                "\n} else {" +
                "\n  statement.bindNull(4 + start);" +
                "\n}",
                builder.build().toString().trim())
    }

    @Test
    fun test_canCombinePackagePrivateCase() {
        val foreignKeyAccessCombiner = ForeignKeyAccessCombiner(PackagePrivateScopeColumnAccessor("name",
                "com.fuzz.android", "_", "TestHelper"))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("test",
                PrimaryReferenceAccessCombiner(PackagePrivateScopeColumnAccessor("test",
                        "com.fuzz.android", "_", "TestHelper2"),
                        TypeName.get(String::class.java)))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(4))

        assertEquals("if (com.fuzz.android.TestHelper_Helper.getName(model) != null) {" +
                "\n  clause.and(test.eq(com.fuzz.android.TestHelper2_Helper.getTest(com.fuzz.android.TestHelper_Helper.getName(model))));" +
                "\n} else {" +
                "\n  clause.and(test.eq((com.raizlabs.android.dbflow.sql.language.IConditional) null));" +
                "\n}",
                builder.build().toString().trim())
    }

    @Test
    fun test_canDoComplexCase() {
        val foreignKeyAccessCombiner = ForeignKeyAccessCombiner(VisibleScopeColumnAccessor("modem"))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("number",
                ContentValuesCombiner(PackagePrivateScopeColumnAccessor("number",
                        "com.fuzz", "\$", "AnotherHelper"),
                        TypeName.INT))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("date",
                ContentValuesCombiner(TypeConverterScopeColumnAccessor("global_converter", "date"),
                        TypeName.get(Date::class.java)))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(1))

        assertEquals("if (model.modem != null) {" +
                "\n  values.put(\"number\", com.fuzz.AnotherHelper\$\$Helper.getNumber(model.modem));" +
                "\n  values.put(\"date\", global_converter.getDBValue(model.modem.date));" +
                "\n} else {" +
                "\n  values.putNull(\"number\");" +
                "\n  values.putNull(\"date\");" +
                "\n}",
                builder.build().toString().trim())
    }
}