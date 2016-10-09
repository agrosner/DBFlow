package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.addStatement
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

abstract class ColumnAccessCombiner(val fieldLevelAccessor: ColumnAccessor,
                                    val fieldTypeName: TypeName,
                                    val wrapperLevelAccessor: ColumnAccessor? = null,
                                    val wrapperFieldTypeName: TypeName? = null) {

    fun getFieldAccessBlock(existingBuilder: CodeBlock.Builder): CodeBlock {
        val modelBlock = CodeBlock.of("model")
        val fieldAccess: CodeBlock
        if (wrapperLevelAccessor != null && !fieldTypeName.isPrimitive) {
            fieldAccess = CodeBlock.of("ref" + fieldLevelAccessor.propertyName)

            existingBuilder.addStatement("\$T \$L = \$L != null ? \$L : null",
                    wrapperFieldTypeName, fieldAccess,
                    fieldLevelAccessor.get(modelBlock),
                    wrapperLevelAccessor.get(fieldLevelAccessor.get(modelBlock)))
        } else {
            if (wrapperLevelAccessor != null) {
                fieldAccess = wrapperLevelAccessor.get(fieldLevelAccessor.get(modelBlock))
            } else {
                fieldAccess = fieldLevelAccessor.get(modelBlock)
            }
        }
        return fieldAccess
    }

    abstract fun addCode(code: CodeBlock.Builder,
                         columnRepresentation: String, defaultValue: CodeBlock? = null,
                         index: Int = -1)

}

class ContentValuesCombiner(fieldLevelAccessor: ColumnAccessor,
                            fieldTypeName: TypeName,
                            wrapperLevelAccessor: ColumnAccessor? = null,
                            wrapperFieldTypeName: TypeName? = null)
: ColumnAccessCombiner(fieldLevelAccessor, fieldTypeName, wrapperLevelAccessor, wrapperFieldTypeName) {

    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int) {
        val fieldAccess: CodeBlock = getFieldAccessBlock(code)
        if (fieldTypeName.isPrimitive) {
            code.addStatement("values.put(\$1S, \$2L)", columnRepresentation, fieldAccess)
        } else {
            code.addStatement("values.put(\$1S, \$2L != null ? \$2L : \$3L)", columnRepresentation, fieldAccess, defaultValue)
        }
    }
}

class SqliteStatementAccessCombiner(fieldLevelAccessor: ColumnAccessor, fieldTypeName: TypeName,
                                    wrapperLevelAccessor: ColumnAccessor? = null,
                                    wrapperFieldTypeName: TypeName? = null)
: ColumnAccessCombiner(fieldLevelAccessor, fieldTypeName, wrapperLevelAccessor, wrapperFieldTypeName) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String, defaultValue: CodeBlock?, index: Int) {
        val fieldAccess: CodeBlock = getFieldAccessBlock(code)

        if (fieldTypeName.isPrimitive) {
            code.addStatement("statement.bind\$L(\$L + \$L, \$L)",
                    SQLiteHelper[fieldTypeName].sqLiteStatementMethod,
                    index, columnRepresentation, fieldAccess)
        } else {
            code.addStatement("statement.bind\$1L(\$2L + \$3L, \$4L != null ? \$4L : \$5L)",
                    SQLiteHelper[fieldTypeName].sqLiteStatementMethod, index, columnRepresentation,
                    fieldAccess, defaultValue)
        }
    }

}

class LoadFromCursorAccessCombiner(fieldLevelAccessor: ColumnAccessor,
                                   fieldTypeName: TypeName,
                                   val orderedCursorLookup: Boolean = false,
                                   val assignDefaultValuesFromCursor: Boolean = true,
                                   wrapperLevelAccessor: ColumnAccessor? = null,
                                   wrapperFieldTypeName: TypeName? = null)
: ColumnAccessCombiner(fieldLevelAccessor, fieldTypeName, wrapperLevelAccessor, wrapperFieldTypeName) {

    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int) {
        val indexName: CodeBlock
        if (!orderedCursorLookup) {
            indexName = CodeBlock.of("index_\$L", columnRepresentation)
            code.addStatement("\$T \$L = cursor.getColumnIndex(\$S)", Int::class.java, indexName,
                    columnRepresentation)
            code.beginControlFlow("if (\$1L != -1 && !cursor.isNull(\$1L))", indexName)
        } else {
            indexName = CodeBlock.of(index.toString())
            code.beginControlFlow("if (!cursor.isNull(\$1L))", index)
        }

        val cursorAccess = CodeBlock.of("cursor.\$L(\$L)",
                SQLiteHelper.getMethod(wrapperFieldTypeName ?: fieldTypeName), indexName)
        if (wrapperLevelAccessor != null) {
            code.addStatement(fieldLevelAccessor.set(wrapperLevelAccessor.set(cursorAccess)))
        } else {
            code.addStatement(fieldLevelAccessor.set(cursorAccess))
        }

        code.nextControlFlow("else")
        code.addStatement(fieldLevelAccessor.set(defaultValue))
        code.endControlFlow()
    }
}