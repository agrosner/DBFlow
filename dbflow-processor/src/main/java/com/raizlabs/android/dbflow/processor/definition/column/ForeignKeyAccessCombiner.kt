package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.utils.statement
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.NameAllocator
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
        columnAccessCombiner.apply {
            code.addCode(columnRepresentation, defaultValue, index, modelAccessBlock)
        }
    }

    fun addNull(code: CodeBlock.Builder, index: Int) {
        columnAccessCombiner.addNull(code, columnRepresentation, index)
    }
}

class ForeignKeyLoadFromCursorCombiner(val fieldAccessor: ColumnAccessor,
                                       val referencedTypeName: TypeName,
                                       val referencedTableTypeName: TypeName,
                                       val isStubbed: Boolean,
                                       val nameAllocator: NameAllocator) {
    var fieldAccesses: List<PartialLoadFromCursorAccessCombiner> = arrayListOf()

    fun addCode(code: CodeBlock.Builder, index: AtomicInteger) {
        val ifChecker = CodeBlock.builder()
        val setterBlock = CodeBlock.builder()

        if (!isStubbed) {
            setterBlock.add("\$T.select().from(\$T.class).where()",
                ClassNames.SQLITE, referencedTypeName)
        } else {
            setterBlock.statement(
                fieldAccessor.set(CodeBlock.of("new \$T()", referencedTypeName), modelBlock))
        }
        for ((i, it) in fieldAccesses.withIndex()) {
            it.addRetrieval(setterBlock, index.get(), referencedTableTypeName, isStubbed, fieldAccessor, nameAllocator)
            it.addColumnIndex(code, index.get(), nameAllocator)
            it.addIndexCheckStatement(ifChecker, index.get(), i == fieldAccesses.size - 1, nameAllocator)

            if (i < fieldAccesses.size - 1) {
                index.incrementAndGet()
            }
        }

        if (!isStubbed) setterBlock.add("\n.querySingle()")

        code.beginControlFlow("if (\$L)", ifChecker.build())
        if (!isStubbed) {
            code.statement(fieldAccessor.set(setterBlock.build(), modelBlock))
        } else {
            code.add(setterBlock.build())
        }
        code.nextControlFlow("else")
            .statement(fieldAccessor.set(CodeBlock.of("null"), modelBlock))
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

    var indexName: CodeBlock? = null

    fun getIndexName(index: Int, nameAllocator: NameAllocator): CodeBlock {
        if (indexName == null) {
            indexName = if (!orderedCursorLookup) {
                CodeBlock.of(nameAllocator.newName("index_$columnRepresentation", columnRepresentation))
            } else {
                CodeBlock.of(index.toString())
            }
        }
        return indexName!!
    }


    fun addRetrieval(code: CodeBlock.Builder, index: Int, referencedTableTypeName: TypeName,
                     isStubbed: Boolean, parentAccessor: ColumnAccessor,
                     nameAllocator: NameAllocator) {
        val cursorAccess = CodeBlock.of("cursor.\$L(\$L)",
            SQLiteHelper.getMethod(subWrapperTypeName ?: fieldTypeName), getIndexName(index, nameAllocator))
        val fieldAccessBlock = subWrapperAccessor?.set(cursorAccess) ?: cursorAccess

        if (!isStubbed) {
            code.add(CodeBlock.of("\n.and(\$T.\$L.eq(\$L))",
                referencedTableTypeName, propertyRepresentation, fieldAccessBlock))
        } else if (fieldLevelAccessor != null) {
            code.statement(fieldLevelAccessor.set(cursorAccess, parentAccessor.get(modelBlock)))
        }

    }

    fun addColumnIndex(code: CodeBlock.Builder, index: Int, nameAllocator: NameAllocator) {
        if (!orderedCursorLookup) {
            code.statement(CodeBlock.of("int \$L = cursor.getColumnIndex(\$S)",
                getIndexName(index, nameAllocator), columnRepresentation))
        }
    }

    fun addIndexCheckStatement(code: CodeBlock.Builder, index: Int,
                               isLast: Boolean, nameAllocator: NameAllocator) {
        if (!orderedCursorLookup) code.add("\$L != -1 && ", getIndexName(index, nameAllocator))

        code.add("!cursor.isNull(\$L)", getIndexName(index, nameAllocator))

        if (!isLast) code.add(" && ")
    }
}