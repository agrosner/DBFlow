package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.utils.addStatement
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
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
            it.addNull(nullAccessBlock, index.get())
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

    fun addNull(code: CodeBlock.Builder, index: Int) {
        columnAccessCombiner.addNull(code, columnRepresentation, index)
    }
}

class ForeignKeyLoadFromCursorCombiner(val fieldAccessor: ColumnAccessor,
                                       val referencedTypeName: TypeName,
                                       val referencedTableTypeName: TypeName) {
    var fieldAccesses: List<PartialLoadFromCursorAccessCombiner> = arrayListOf()

    fun addCode(code: CodeBlock.Builder, index: AtomicInteger) {
        val modelBlock = CodeBlock.of("model")

        val ifChecker = CodeBlock.builder()
        val selectBlock = CodeBlock.builder()
                .add("\$T.select().from(\$T.class).where()", ClassNames.SQLITE, referencedTypeName)
        for ((i, it) in fieldAccesses.withIndex()) {
            it.addCondition(selectBlock, index.get(), referencedTableTypeName)
            it.addColumnIndex(code, index.get())

            it.addIndexCheckStatement(ifChecker, index.get(), i == fieldAccesses.size - 1)

            index.incrementAndGet()
        }

        selectBlock.add("\n.querySingle()")

        code.beginControlFlow("if (\$L)", ifChecker.build())
        code.addStatement(fieldAccessor.set(selectBlock.build(), modelBlock))
        code.nextControlFlow("else")
                .addStatement(fieldAccessor.set(CodeBlock.of("null"), modelBlock))
                .endControlFlow()
    }
}

class PartialLoadFromCursorAccessCombiner(
        val columnRepresentation: String,
        val propertyRepresentation: String,
        val fieldTypeName: TypeName,
        val orderedCursorLookup: Boolean = false,
        val subWrapperAccessor: ColumnAccessor? = null) {

    fun getIndexName(index: Int): CodeBlock {
        return if (!orderedCursorLookup) {
            CodeBlock.of("index_\$L", columnRepresentation)
        } else {
            CodeBlock.of(index.toString())
        }
    }


    fun addCondition(code: CodeBlock.Builder, index: Int, referencedTableTypeName: TypeName) {
        val cursorAccess = CodeBlock.of("cursor.\$L(\$L)",
                SQLiteHelper.getMethod(fieldTypeName), getIndexName(index))

        val fieldAccessBlock: CodeBlock
        if (subWrapperAccessor != null) {
            fieldAccessBlock = subWrapperAccessor.set(cursorAccess)
        } else {
            fieldAccessBlock = cursorAccess
        }

        code.add(CodeBlock.builder().add("\n.and(\$T.\$L.eq(\$L))", referencedTableTypeName,
                propertyRepresentation, fieldAccessBlock).build())

    }

    fun addColumnIndex(code: CodeBlock.Builder, index: Int) {
        if (!orderedCursorLookup) {
            code.addStatement(CodeBlock.of("int \$L = cursor.getColumnIndex(\$S)",
                    getIndexName(index), columnRepresentation))
        }
    }

    fun addIndexCheckStatement(code: CodeBlock.Builder, index: Int,
                               isLast: Boolean) {
        if (!orderedCursorLookup) {
            code.add("\$L != -1 && ", getIndexName(index))
        }
        code.add("!cursor.isNull(\$L)", getIndexName(index))

        if (!isLast) {
            code.add(" && ")
        }
    }
}