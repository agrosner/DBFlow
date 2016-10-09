package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.processor.SQLiteHelper
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
                         columnRepresentation: String, defaultValue: CodeBlock? = null)

}

class ContentValuesCombiner(fieldLevelAccessor: ColumnAccessor,
                            fieldTypeName: TypeName,
                            wrapperLevelAccessor: ColumnAccessor? = null,
                            wrapperFieldTypeName: TypeName? = null)
: ColumnAccessCombiner(fieldLevelAccessor, fieldTypeName, wrapperLevelAccessor, wrapperFieldTypeName) {

    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?) {
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
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String, defaultValue: CodeBlock?) {
        val fieldAccess: CodeBlock = getFieldAccessBlock(code)

        if (fieldTypeName.isPrimitive) {
            code.addStatement("statement.bind\$L(\$L, \$L)",
                    SQLiteHelper[fieldTypeName].sqLiteStatementMethod,
                    columnRepresentation, fieldAccess)
        } else {
            code.addStatement("statement.bind\$1L(\$2L, \$3L != null ? \$3L : \$4L)",
                    SQLiteHelper[fieldTypeName].sqLiteStatementMethod, columnRepresentation, fieldAccess,
                    defaultValue)
        }
    }

}