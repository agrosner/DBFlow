package com.raizlabs.android.dbflow.processor.definition.column

import com.squareup.javapoet.CodeBlock
import java.util.concurrent.atomic.AtomicInteger

/**
 * Description: Provides structured way to combine ForeignKey for both SQLiteStatement and ContentValues
 * bindings.
 *
 * @author Andrew Grosner (fuzz)
 */
class ForeignKeyAccessCombiner(val fieldAccessor: ColumnAccessor) {

    var fieldAccesses: List<ForeignKeyAccessField> = arrayListOf()

    fun addCode(code: CodeBlock.Builder, index: AtomicInteger) {
        val modelAccessBlock = fieldAccessor.get(CodeBlock.of("model"))
        code.beginControlFlow("if (\$L != null)", modelAccessBlock)
        val nullAccessBlock = CodeBlock.builder()
        fieldAccesses.forEach {
            it.addCode(code, index.get(), modelAccessBlock)
            it.addNull(nullAccessBlock)
            index.incrementAndGet()
        }
        code.nextControlFlow("else")
                .add(nullAccessBlock.build().toString())
                .endControlFlow()
    }
}

data class ForeignKeyAccessField(
        val columnRepresentation: String,
        val columnAccessCombiner: ColumnAccessCombiner,
        val defaultValue: CodeBlock? = null) {

    fun addCode(code: CodeBlock.Builder, index: Int,
                modelAccessBlock: CodeBlock) {
        columnAccessCombiner.addCode(code, columnRepresentation, defaultValue, index,
                modelAccessBlock)
    }

    fun addNull(code: CodeBlock.Builder) {
        columnAccessCombiner.addNull(code, columnRepresentation)
    }
}