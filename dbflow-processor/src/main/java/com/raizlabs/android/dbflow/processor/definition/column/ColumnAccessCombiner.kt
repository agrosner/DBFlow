package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.utils.addStatement
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

abstract class ColumnAccessCombiner(val fieldLevelAccessor: ColumnAccessor,
                                    val fieldTypeName: TypeName,
                                    val wrapperLevelAccessor: ColumnAccessor? = null,
                                    val wrapperFieldTypeName: TypeName? = null,
                                    val subWrapperAccessor: ColumnAccessor? = null) {

    fun getFieldAccessBlock(existingBuilder: CodeBlock.Builder,
                            modelBlock: CodeBlock): CodeBlock {
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
                         index: Int = -1,
                         modelBlock: CodeBlock = CodeBlock.of("model"))

    open fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int = -1) {

    }

    protected fun useStoredFieldRef(): Boolean {
        return wrapperLevelAccessor == null && fieldLevelAccessor !is VisibleScopeColumnAccessor
    }

}

class ContentValuesCombiner(fieldLevelAccessor: ColumnAccessor,
                            fieldTypeName: TypeName,
                            wrapperLevelAccessor: ColumnAccessor? = null,
                            wrapperFieldTypeName: TypeName? = null,
                            subWrapperAccessor: ColumnAccessor? = null)
: ColumnAccessCombiner(fieldLevelAccessor, fieldTypeName, wrapperLevelAccessor,
        wrapperFieldTypeName, subWrapperAccessor) {

    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int,
                         modelBlock: CodeBlock) {
        val fieldAccess: CodeBlock = getFieldAccessBlock(code, modelBlock)
        if (fieldTypeName.isPrimitive) {
            code.addStatement("values.put(\$1S, \$2L)", columnRepresentation, fieldAccess)
        } else {
            if (defaultValue != null) {
                var storedFieldAccess = CodeBlock.of("ref\$L", fieldLevelAccessor.propertyName)
                if (useStoredFieldRef()) {
                    code.addStatement("\$T \$L = \$L", fieldTypeName, storedFieldAccess, fieldAccess)
                } else {
                    storedFieldAccess = fieldAccess
                }
                var subWrapperFieldAccess = storedFieldAccess
                if (subWrapperAccessor != null) {
                    subWrapperFieldAccess = subWrapperAccessor.get(storedFieldAccess)
                }
                code.addStatement("values.put(\$S, \$L != null ? \$L : \$L)", columnRepresentation, storedFieldAccess, subWrapperFieldAccess, defaultValue)
            } else {
                code.addStatement("values.put(\$S, \$L)", columnRepresentation, fieldAccess)
            }
        }
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement("values.putNull(\$S)", columnRepresentation)
    }
}

class SqliteStatementAccessCombiner(fieldLevelAccessor: ColumnAccessor, fieldTypeName: TypeName,
                                    wrapperLevelAccessor: ColumnAccessor? = null,
                                    wrapperFieldTypeName: TypeName? = null,
                                    subWrapperAccessor: ColumnAccessor? = null)
: ColumnAccessCombiner(fieldLevelAccessor, fieldTypeName, wrapperLevelAccessor,
        wrapperFieldTypeName, subWrapperAccessor) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int,
                         modelBlock: CodeBlock) {
        val fieldAccess: CodeBlock = getFieldAccessBlock(code, modelBlock)

        if (fieldTypeName.isPrimitive) {
            code.addStatement("statement.bind\$L(\$L + \$L, \$L)",
                    SQLiteHelper[fieldTypeName].sqLiteStatementMethod,
                    index, columnRepresentation, fieldAccess)
        } else {
            if (defaultValue != null) {
                var storedFieldAccess = CodeBlock.of("ref\$L", fieldLevelAccessor.propertyName)

                if (useStoredFieldRef()) {
                    code.addStatement("\$T \$L = \$L", fieldTypeName, storedFieldAccess, fieldAccess)
                } else {
                    storedFieldAccess = fieldAccess
                }

                var subWrapperFieldAccess = storedFieldAccess
                if (subWrapperAccessor != null) {
                    subWrapperFieldAccess = subWrapperAccessor.get(storedFieldAccess)
                }
                code.addStatement("statement.bind\$L(\$L + \$L, \$L != null ? \$L : \$L)",
                        SQLiteHelper[wrapperFieldTypeName ?: fieldTypeName].sqLiteStatementMethod, index, columnRepresentation,
                        storedFieldAccess, subWrapperFieldAccess, defaultValue)
            } else {
                code.addStatement("statement.bind\$L(\$L + \$L, \$L)",
                        SQLiteHelper[wrapperFieldTypeName ?: fieldTypeName].sqLiteStatementMethod, index,
                        columnRepresentation, fieldAccess)
            }
        }
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement("statement.bindNull(\$L + \$L)", index, columnRepresentation)
    }
}

class LoadFromCursorAccessCombiner(fieldLevelAccessor: ColumnAccessor,
                                   fieldTypeName: TypeName,
                                   val orderedCursorLookup: Boolean = false,
                                   val assignDefaultValuesFromCursor: Boolean = true,
                                   wrapperLevelAccessor: ColumnAccessor? = null,
                                   wrapperFieldTypeName: TypeName? = null,
                                   subWrapperAccessor: ColumnAccessor? = null)
: ColumnAccessCombiner(fieldLevelAccessor, fieldTypeName, wrapperLevelAccessor,
        wrapperFieldTypeName, subWrapperAccessor) {

    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int,
                         modelBlock: CodeBlock) {
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
            if (subWrapperAccessor != null) {
                code.addStatement(fieldLevelAccessor.set(
                        wrapperLevelAccessor.set(subWrapperAccessor.set(cursorAccess)), modelBlock))
            } else {
                code.addStatement(fieldLevelAccessor.set(
                        wrapperLevelAccessor.set(cursorAccess), modelBlock))
            }
        } else {
            code.addStatement(fieldLevelAccessor.set(cursorAccess, modelBlock))
        }

        if (assignDefaultValuesFromCursor) {
            code.nextControlFlow("else")
            if (wrapperLevelAccessor != null) {
                code.addStatement(fieldLevelAccessor.set(wrapperLevelAccessor.set(defaultValue,
                        isDefault = true), modelBlock))
            } else {
                code.addStatement(fieldLevelAccessor.set(defaultValue, modelBlock))
            }
        }
        code.endControlFlow()
    }
}

class PrimaryReferenceAccessCombiner(fieldLevelAccessor: ColumnAccessor,
                                     fieldTypeName: TypeName,
                                     wrapperLevelAccessor: ColumnAccessor? = null,
                                     wrapperFieldTypeName: TypeName? = null,
                                     subWrapperAccessor: ColumnAccessor? = null)
: ColumnAccessCombiner(fieldLevelAccessor, fieldTypeName, wrapperLevelAccessor,
        wrapperFieldTypeName, subWrapperAccessor) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int,
                         modelBlock: CodeBlock) {
        code.addStatement("clause.and(\$L.eq(\$L))", columnRepresentation,
                getFieldAccessBlock(code, modelBlock))
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement("clause.and(\$L.eq((\$T) \$L))", columnRepresentation,
                ClassNames.ICONDITIONAL, "null")
    }
}