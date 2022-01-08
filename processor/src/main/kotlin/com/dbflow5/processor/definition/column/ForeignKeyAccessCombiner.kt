package com.dbflow5.processor.definition.column

import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.SQLiteHelper
import com.dbflow5.processor.utils.statement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.TypeName
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.internal.Reflection

/**
 * Description: Provides structured way to combine ForeignKey for both SQLiteStatement and ContentValues
 * bindings.
 *
 * @author Andrew Grosner (fuzz)
 */
class ForeignKeyAccessCombiner(private val fieldAccessor: ColumnAccessor) {

    var fieldAccesses: List<ForeignKeyAccessField> = arrayListOf()

    fun addCode(
        code: CodeBlock.Builder, index: AtomicInteger, useStart: Boolean = true,
        defineProperty: Boolean = true
    ) {
        val modelAccessBlock = fieldAccessor.get(modelBlock)
        code.beginControlFlow("if (\$L != null)", modelAccessBlock)
        val nullAccessBlock = CodeBlock.builder()
        for ((i, field) in fieldAccesses.withIndex()) {
            field.addCode(code, index.get(), modelAccessBlock, useStart, defineProperty)
            field.addNull(nullAccessBlock, index.get(), useStart)

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

class ForeignKeyAccessField(
    private val columnRepresentation: String,
    private val columnAccessCombiner: ColumnAccessCombiner,
    private val defaultValue: CodeBlock? = null
) {

    fun addCode(
        code: CodeBlock.Builder, index: Int, modelAccessBlock: CodeBlock,
        useStart: Boolean = true,
        defineProperty: Boolean = true
    ) {
        columnAccessCombiner.apply {
            code.addCode(
                if (useStart) columnRepresentation else "", defaultValue, index,
                modelAccessBlock, defineProperty
            )
        }
    }

    fun addNull(code: CodeBlock.Builder, index: Int, useStart: Boolean) {
        columnAccessCombiner.addNull(code, if (useStart) columnRepresentation else "", index)
    }
}

class ForeignKeyLoadFromCursorCombiner(
    private val fieldAccessor: ColumnAccessor,
    private val referencedTypeName: TypeName,
    private val referencedTableTypeName: TypeName,
    private val isStubbed: Boolean,
    private val nameAllocator: NameAllocator
) {
    var fieldAccesses: List<PartialLoadFromCursorAccessCombiner> = arrayListOf()

    fun addCode(code: CodeBlock.Builder, index: AtomicInteger) {
        val ifChecker = CodeBlock.builder()
        val setterBlock = CodeBlock.builder()

        if (!isStubbed) {
            setterBlock.add(
                "\$T.select().from(\$T.getKotlinClass(\$T.class)).where()",
                ClassNames.SQLITE,
                ClassNames.JVM_CLASS_MAPPING,
                referencedTypeName
            )
        } else {
            setterBlock.statement(
                fieldAccessor.set(CodeBlock.of("new \$T()", referencedTypeName), modelBlock)
            )
        }
        for ((i, it) in fieldAccesses.withIndex()) {
            it.addRetrieval(
                setterBlock,
                index.get(),
                referencedTableTypeName,
                isStubbed,
                fieldAccessor,
                nameAllocator
            )
            it.addColumnIndex(code, index.get(), referencedTableTypeName, nameAllocator)
            it.addIndexCheckStatement(
                ifChecker, index.get(), referencedTableTypeName,
                i == fieldAccesses.size - 1, nameAllocator
            )

            if (i < fieldAccesses.size - 1) {
                index.incrementAndGet()
            }
        }

        if (!isStubbed) setterBlock.add("\n.querySingle(wrapper)")

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
    private val columnRepresentation: String,
    private val propertyRepresentation: String,
    private val fieldTypeName: TypeName,
    private val orderedCursorLookup: Boolean = false,
    private val fieldLevelAccessor: ColumnAccessor? = null,
    private val subWrapperAccessor: ColumnAccessor? = null,
    private val subWrapperTypeName: TypeName? = null
) {

    private var indexName: CodeBlock? = null

    private fun getIndexName(
        index: Int,
        nameAllocator: NameAllocator,
        referencedTypeName: TypeName
    ): CodeBlock {
        if (indexName == null) {
            indexName = if (!orderedCursorLookup) {
                // post fix with referenced type name simple name
                CodeBlock.of(
                    nameAllocator.newName(
                        "index_${columnRepresentation}_" +
                            if (referencedTypeName is ClassName) referencedTypeName.simpleName() else "",
                        columnRepresentation
                    )
                )
            } else {
                CodeBlock.of(index.toString())
            }
        }
        return indexName!!
    }


    fun addRetrieval(
        code: CodeBlock.Builder, index: Int, referencedTableTypeName: TypeName,
        isStubbed: Boolean, parentAccessor: ColumnAccessor,
        nameAllocator: NameAllocator
    ) {
        val cursorAccess = CodeBlock.of(
            "cursor.\$L(\$L)",
            SQLiteHelper.getMethod(subWrapperTypeName ?: fieldTypeName),
            getIndexName(index, nameAllocator, referencedTableTypeName)
        )
        val fieldAccessBlock = subWrapperAccessor?.set(cursorAccess) ?: cursorAccess

        if (!isStubbed) {
            code.add(
                CodeBlock.of(
                    "\n.and(\$T.\$L.eq(\$L))",
                    referencedTableTypeName, propertyRepresentation, fieldAccessBlock
                )
            )
        } else if (fieldLevelAccessor != null) {
            code.statement(fieldLevelAccessor.set(fieldAccessBlock, parentAccessor.get(modelBlock)))
        }
    }

    fun addColumnIndex(
        code: CodeBlock.Builder, index: Int,
        referencedTableTypeName: TypeName,
        nameAllocator: NameAllocator
    ) {
        if (!orderedCursorLookup) {
            code.statement(
                CodeBlock.of(
                    "int \$L = cursor.getColumnIndex(\$S)",
                    getIndexName(index, nameAllocator, referencedTableTypeName),
                    columnRepresentation
                )
            )
        }
    }

    fun addIndexCheckStatement(
        code: CodeBlock.Builder, index: Int,
        referencedTableTypeName: TypeName,
        isLast: Boolean, nameAllocator: NameAllocator
    ) {
        val indexName = getIndexName(index, nameAllocator, referencedTableTypeName)
        if (!orderedCursorLookup) code.add("$indexName != -1 && ")

        code.add("!cursor.isNull($indexName)")

        if (!isLast) code.add(" && ")
    }
}