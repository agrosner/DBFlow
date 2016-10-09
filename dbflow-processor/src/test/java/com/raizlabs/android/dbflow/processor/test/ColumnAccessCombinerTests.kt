package com.raizlabs.android.dbflow.processor.test

import com.raizlabs.android.dbflow.processor.definition.column.ContentValuesCombiner
import com.raizlabs.android.dbflow.processor.definition.column.VisibleScopeColumnAccessor
import com.squareup.javapoet.CodeBlock
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
        val combiner = ContentValuesCombiner(VisibleScopeColumnAccessor("name"))

        val codeBuilder = CodeBlock.builder()
        combiner.addCode(codeBuilder, "columnName", CodeBlock.of("\$S", "nonNull"))

        assertEquals("values.put(\"columnName\", model.name != null ? model.name : \"nonNull\")",
                codeBuilder.build().toString())
    }
}