package com.raizlabs.android.dbflow.processor.test

import com.raizlabs.android.dbflow.processor.definition.column.*
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import org.junit.Assert
import org.junit.Test
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

        Assert.assertEquals("if (model.name != null) {" +
                "\n  values.put(\"test\", model.name.test != null ? model.name.test : null);" +
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
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("test",
                ContentValuesCombiner(VisibleScopeColumnAccessor("test"), TypeName.get(String::class.java)))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(4))

        Assert.assertEquals("if (model.getName() != null) {" +
                "\n  values.put(\"test\", model.getName().test != null ? model.getName().test : null);" +
                "\n} else {" +
                "\n  values.putNull(\"test\");" +
                "\n}",
                builder.build().toString().trim())
    }

    @Test
    fun test_canCombinePackagePrivateCase() {
        val foreignKeyAccessCombiner = ForeignKeyAccessCombiner(PackagePrivateScopeColumnAccessor("name",
                "com.fuzz.android", "_", "TestHelper"))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("test",
                ContentValuesCombiner(VisibleScopeColumnAccessor("test"), TypeName.get(String::class.java)))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(4))

        Assert.assertEquals("if (com.fuzz.android.TestHelper_Helper.getName(model) != null) {" +
                "\n  values.put(\"test\", com.fuzz.android.TestHelper_Helper.getName(model).test != null ? com.fuzz.android.TestHelper_Helper.getName(model).test : null);" +
                "\n} else {" +
                "\n  values.putNull(\"test\");" +
                "\n}",
                builder.build().toString().trim())
    }
}