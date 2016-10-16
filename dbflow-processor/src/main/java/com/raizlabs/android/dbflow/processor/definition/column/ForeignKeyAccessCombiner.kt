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
        val modelAccessBlock = fieldAccessor.get(modelBlock)
        code.beginControlFlow("if (\$L != null)", modelAccessBlock)
        val nullAccessBlock = CodeBlock.builder()
        for ((i, it) in fieldAccesses.withIndex()) {
            it.addCode(code, index.get(), modelAccessBlock)
            it.addNull(nullAccessBlock, index.get())

            // do not increment last
            if (i < fieldAccesses.size - 1) {
                index.incrementAndGet()
            }
        }
        code.nextControlFlow("else")
                .add(nullAccessBlock.build().toString())
                .endControlFlow()
    }
}

class ForeignKeyAccessField(val columnRepresentation: String,
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
                                       val referencedTableTypeName: TypeName,
                                       val isStubbed: Boolean) {
    var fieldAccesses: List<PartialLoadFromCursorAccessCombiner> = arrayListOf()

    fun addCode(code: CodeBlock.Builder, index: AtomicInteger) {
        val ifChecker = CodeBlock.builder()
        val setterBlock = CodeBlock.builder()

        if (!isStubbed) {
            setterBlock.add("\$T.select().from(\$T.class).where()",
                    ClassNames.SQLITE, referencedTypeName)
        } else {
            setterBlock.addStatement(
                    fieldAccessor.set(CodeBlock.of("new \$T()", referencedTypeName), modelBlock))
        }
        for ((i, it) in fieldAccesses.withIndex()) {
            it.addRetrieval(setterBlock, index.get(), referencedTableTypeName, isStubbed, fieldAccessor)
            it.addColumnIndex(code, index.get())
            it.addIndexCheckStatement(ifChecker, index.get(), i == fieldAccesses.size - 1)
            index.incrementAndGet()
        }

        if (!isStubbed) setterBlock.add("\n.querySingle()")

        code.beginControlFlow("if (\$L)", ifChecker.build())
        if (!isStubbed) {
            code.addStatement(fieldAccessor.set(setterBlock.build(), modelBlock))
        } else {
            code.add(setterBlock.build())
        }
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
        val fieldLevelAccessor: ColumnAccessor? = null,
        val subWrapperAccessor: ColumnAccessor? = null,
        val subWrapperTypeName: TypeName? = null) {

    fun getIndexName(index: Int): CodeBlock {
        return if (!orderedCursorLookup) {
            CodeBlock.of("index_\$L", columnRepresentation)
        } else {
            CodeBlock.of(index.toString())
        }
    }


    fun addRetrieval(code: CodeBlock.Builder, index: Int, referencedTableTypeName: TypeName,
                     isStubbed: Boolean, parentAccessor: ColumnAccessor) {
        val cursorAccess = CodeBlock.of("cursor.\$L(\$L)",
                SQLiteHelper.getMethod(subWrapperTypeName ?: fieldTypeName), getIndexName(index))
        val fieldAccessBlock = subWrapperAccessor?.set(cursorAccess) ?: cursorAccess

        if (!isStubbed) {
            code.add(CodeBlock.of("\n.and(\$T.\$L.eq(\$L))",
                    referencedTableTypeName, propertyRepresentation, fieldAccessBlock))
        } else if (fieldLevelAccessor != null) {
            code.addStatement(fieldLevelAccessor.set(cursorAccess, parentAccessor.get(modelBlock)))
        }

    }

    fun addColumnIndex(code: CodeBlock.Builder, index: Int) {
        if (!orderedCursorLookup) {
            code.addStatement(CodeBlock.of("int \$L = cursor.getColumnIndex(\$S)",
                    getIndexName(index), columnRepresentation))
        }
    }

    fun addIndexCheckStatement(code: CodeBlock.Builder, index: Int,
                               isLast: Boolean) {
        if (!orderedCursorLookup) code.add("\$L != -1 && ", getIndexName(index))

        code.add("!cursor.isNull(\$L)", getIndexName(index))

        if (!isLast) code.add(" && ")
    }
}