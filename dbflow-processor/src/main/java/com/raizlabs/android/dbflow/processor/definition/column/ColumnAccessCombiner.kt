package com.raizlabs.android.dbflow.processor.definition.column

import com.grosner.kpoet.S
import com.grosner.kpoet.`else`
import com.grosner.kpoet.`if`
import com.grosner.kpoet.statement
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.addStatement
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

data class Combiner(val fieldLevelAccessor: ColumnAccessor,
                    val fieldTypeName: TypeName,
                    val wrapperLevelAccessor: ColumnAccessor? = null,
                    val wrapperFieldTypeName: TypeName? = null,
                    val subWrapperAccessor: ColumnAccessor? = null)

abstract class ColumnAccessCombiner(val combiner: Combiner) {

    fun getFieldAccessBlock(existingBuilder: CodeBlock.Builder,
                            modelBlock: CodeBlock,
                            useWrapper: Boolean = true): CodeBlock {
        var fieldAccess: CodeBlock = CodeBlock.of("")
        combiner.apply {
            if (wrapperLevelAccessor != null && !fieldTypeName.isPrimitive) {
                fieldAccess = CodeBlock.of("ref" + fieldLevelAccessor.propertyName)

                existingBuilder.addStatement("\$T \$L = \$L != null ? \$L : null",
                        wrapperFieldTypeName, fieldAccess,
                        fieldLevelAccessor.get(modelBlock),
                        wrapperLevelAccessor.get(fieldLevelAccessor.get(modelBlock)))
            } else {
                if (useWrapper && wrapperLevelAccessor != null) {
                    fieldAccess = wrapperLevelAccessor.get(fieldLevelAccessor.get(modelBlock))
                } else {
                    fieldAccess = fieldLevelAccessor.get(modelBlock)
                }
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
}

class SimpleAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock) {
        code.addStatement("return \$L", getFieldAccessBlock(code, modelBlock))
    }

}

class ExistenceAccessCombiner(combiner: Combiner,
                              val autoRowId: Boolean,
                              val quickCheckPrimaryKey: Boolean,
                              val tableClassName: ClassName)
    : ColumnAccessCombiner(combiner) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock) {

        combiner.apply {
            if (autoRowId) {
                val access = getFieldAccessBlock(code, modelBlock)

                code.add("return ")

                if (!fieldTypeName.isPrimitive) {
                    code.add("(\$L != null && ", access)
                }
                code.add("\$L > 0", access)

                if (!fieldTypeName.isPrimitive) {
                    code.add(" || \$L == null)", access)
                }
            }

            if (!autoRowId || !quickCheckPrimaryKey) {
                if (autoRowId) {
                    code.add("\n&& ")
                } else {
                    code.add("return ")
                }

                code.add("\$T.selectCountOf()\n.from(\$T.class)\n.where(getPrimaryConditionClause(\$L))\n.hasData(wrapper)",
                        ClassNames.SQLITE, tableClassName, modelBlock)
            }
            code.add(";\n")
        }
    }

}

class ContentValuesCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {

    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int,
                         modelBlock: CodeBlock) {
        combiner.apply {
            val fieldAccess: CodeBlock = getFieldAccessBlock(code, modelBlock)
            if (fieldTypeName.isPrimitive) {
                code.addStatement("values.put(\$1S, \$2L)", QueryBuilder.quote(columnRepresentation), fieldAccess)
            } else {
                if (defaultValue != null) {
                    val storedFieldAccess = fieldAccess
                    var subWrapperFieldAccess = storedFieldAccess
                    if (subWrapperAccessor != null) {
                        subWrapperFieldAccess = subWrapperAccessor.get(storedFieldAccess)
                    }
                    code.addStatement("values.put(\$S, \$L != null ? \$L : \$L)",
                            QueryBuilder.quote(columnRepresentation), storedFieldAccess, subWrapperFieldAccess, defaultValue)
                } else {
                    code.addStatement("values.put(\$S, \$L)",
                            QueryBuilder.quote(columnRepresentation), fieldAccess)
                }
            }
        }
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement("values.putNull(\$S)", columnRepresentation)
    }
}

class SqliteStatementAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int,
                         modelBlock: CodeBlock) {
        combiner.apply {
            val fieldAccess: CodeBlock = getFieldAccessBlock(code, modelBlock)
            val wrapperMethod = SQLiteHelper[wrapperFieldTypeName ?: fieldTypeName].sqliteStatementWrapperMethod
            val statementMethod = SQLiteHelper[fieldTypeName].sqLiteStatementMethod

            code.apply {
                if (fieldTypeName.isPrimitive) {
                    statement("statement.bind$statementMethod($index + $columnRepresentation, $fieldAccess)")
                } else {
                    val subWrapperFieldAccess = subWrapperAccessor?.get(fieldAccess) ?: fieldAccess
                    if (!defaultValue.toString().isNullOrEmpty()) {
                        `if`("$fieldAccess != null") {
                            statement("statement.bind$wrapperMethod($index + $columnRepresentation," +
                                    " $subWrapperFieldAccess)")
                        }.`else` {
                            statement("statement.bind$statementMethod($index + $columnRepresentation," +
                                    " $defaultValue)")
                        }
                    } else {
                        statement("statement.bind${wrapperMethod}OrNull($index + $columnRepresentation," +
                                " $subWrapperFieldAccess)")
                    }
                }
            }
        }
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement("statement.bindNull(\$L + \$L)", index, columnRepresentation)
    }
}

class LoadFromCursorAccessCombiner(combiner: Combiner,
                                   val orderedCursorLookup: Boolean = false,
                                   val assignDefaultValuesFromCursor: Boolean = true)
    : ColumnAccessCombiner(combiner) {

    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int,
                         modelBlock: CodeBlock) {
        combiner.apply {
            var indexName: CodeBlock
            if (!orderedCursorLookup) {
                indexName = CodeBlock.of(columnRepresentation.S)
            } else {
                indexName = CodeBlock.of(index.toString())
            }

            if (wrapperLevelAccessor != null) {
                if (!orderedCursorLookup) {
                    indexName = CodeBlock.of("index_\$L", columnRepresentation)
                    code.addStatement("\$T \$L = cursor.getColumnIndex(\$S)", Int::class.java, indexName,
                            columnRepresentation)
                    code.beginControlFlow("if (\$1L != -1 && !cursor.isNull(\$1L))", indexName)
                } else {
                    code.beginControlFlow("if (!cursor.isNull(\$1L))", index)
                }
                val cursorAccess = CodeBlock.of("cursor.\$L(\$L)",
                        SQLiteHelper.getMethod(wrapperFieldTypeName ?: fieldTypeName), indexName)
                // special case where we need to append try catch hack
                val isEnum = wrapperLevelAccessor is EnumColumnAccessor
                if (isEnum) {
                    code.beginControlFlow("try")
                }
                if (subWrapperAccessor != null) {
                    code.addStatement(fieldLevelAccessor.set(
                            wrapperLevelAccessor.set(subWrapperAccessor.set(cursorAccess)), modelBlock))
                } else {
                    code.addStatement(fieldLevelAccessor.set(
                            wrapperLevelAccessor.set(cursorAccess), modelBlock))
                }
                if (isEnum) {
                    code.nextControlFlow("catch (\$T i)", IllegalArgumentException::class.java)
                    if (assignDefaultValuesFromCursor) {
                        code.addStatement(fieldLevelAccessor.set(wrapperLevelAccessor.set(defaultValue,
                                isDefault = true), modelBlock))
                    } else {
                        code.addStatement(fieldLevelAccessor.set(defaultValue, modelBlock))
                    }
                    code.endControlFlow()
                }
                if (assignDefaultValuesFromCursor) {
                    code.nextControlFlow("else")
                    code.addStatement(fieldLevelAccessor.set(wrapperLevelAccessor.set(defaultValue,
                            isDefault = true), modelBlock))
                }
                code.endControlFlow()
            } else {

                var defaultValueBlock = defaultValue
                if (!assignDefaultValuesFromCursor) {
                    defaultValueBlock = fieldLevelAccessor.get(modelBlock)
                }
                val cursorAccess = CodeBlock.of("cursor.\$LOrDefault(\$L, $defaultValueBlock)",
                        SQLiteHelper.getMethod(wrapperFieldTypeName ?: fieldTypeName), indexName)
                code.addStatement(fieldLevelAccessor.set(cursorAccess, modelBlock))
            }
        }
    }
}

class PrimaryReferenceAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int,
                         modelBlock: CodeBlock) {
        val wrapperLevelAccessor = this.combiner.wrapperLevelAccessor
        code.addStatement("clause.and(\$L.\$Leq(\$L))", columnRepresentation,
                if (!wrapperLevelAccessor.isPrimitiveTarget()) "invertProperty()." else "",
                getFieldAccessBlock(code, modelBlock, wrapperLevelAccessor !is BooleanColumnAccessor))
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement("clause.and(\$L.eq((\$T) \$L))", columnRepresentation,
                ClassNames.ICONDITIONAL, "null")
    }
}

class UpdateAutoIncrementAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock) {
        combiner.apply {
            var method = ""
            if (SQLiteHelper.containsNumberMethod(fieldTypeName.unbox())) {
                method = fieldTypeName.unbox().toString()
            }

            code.addStatement(fieldLevelAccessor.set(CodeBlock.of("id.\$LValue()", method), modelBlock))
        }
    }

}

class CachingIdAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock) {
        code.addStatement("inValues[\$L] = \$L", index, getFieldAccessBlock(code, modelBlock))
    }

}

class SaveModelAccessCombiner(combiner: Combiner,
                              val implementsModel: Boolean,
                              val extendsBaseModel: Boolean)
    : ColumnAccessCombiner(combiner) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock) {
        combiner.apply {
            val access = getFieldAccessBlock(code, modelBlock)
            code.beginControlFlow("if (\$L != null)", access)
            if (implementsModel) {
                code.addStatement("\$L.save(\$L)", access,
                        if (extendsBaseModel) ModelUtils.wrapper else "")
            } else {
                code.addStatement("\$T.getModelAdapter(\$T.class).save(\$L, \$L)",
                        ClassNames.FLOW_MANAGER, fieldTypeName, access, ModelUtils.wrapper)
            }
            code.endControlFlow()
        }
    }

}

class DeleteModelAccessCombiner(combiner: Combiner,
                                val implementsModel: Boolean,
                                val extendsBaseModel: Boolean)
    : ColumnAccessCombiner(combiner) {
    override fun addCode(code: CodeBlock.Builder, columnRepresentation: String,
                         defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock) {
        combiner.apply {
            val access = getFieldAccessBlock(code, modelBlock)
            code.beginControlFlow("if (\$L != null)", access)
            if (implementsModel) {
                code.addStatement("\$L.delete(\$L)", access,
                        if (extendsBaseModel) ModelUtils.wrapper else "")
            } else {
                code.addStatement("\$T.getModelAdapter(\$T.class).delete(\$L, \$L)",
                        ClassNames.FLOW_MANAGER, fieldTypeName, access, ModelUtils.wrapper)
            }
            code.endControlFlow()
        }
    }

}