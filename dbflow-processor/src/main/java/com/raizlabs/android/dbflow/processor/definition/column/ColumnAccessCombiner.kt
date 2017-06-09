package com.raizlabs.android.dbflow.processor.definition.column

import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.catch
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.processor.utils.statement
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.TypeName

data class Combiner(val fieldLevelAccessor: ColumnAccessor,
                    val fieldTypeName: TypeName,
                    val wrapperLevelAccessor: ColumnAccessor? = null,
                    val wrapperFieldTypeName: TypeName? = null,
                    val subWrapperAccessor: ColumnAccessor? = null,
                    val customPrefixName: String = "")

abstract class ColumnAccessCombiner(val combiner: Combiner) {

    private val nameAllocator = NameAllocator()

    fun getFieldAccessBlock(existingBuilder: CodeBlock.Builder,
                            modelBlock: CodeBlock,
                            useWrapper: Boolean = true,
                            defineProperty: Boolean = true): CodeBlock {
        var fieldAccess: CodeBlock = CodeBlock.of("")
        combiner.apply {
            if (wrapperLevelAccessor != null && !fieldTypeName.isPrimitive) {
                fieldAccess = CodeBlock.of("${nameAllocator.newName(customPrefixName)}ref" + fieldLevelAccessor.propertyName)

                if (defineProperty) {
                    existingBuilder.addStatement("\$T \$L = \$L != null ? \$L : null",
                        wrapperFieldTypeName, fieldAccess,
                        fieldLevelAccessor.get(modelBlock),
                        wrapperLevelAccessor.get(fieldLevelAccessor.get(modelBlock)))
                }
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

    abstract fun CodeBlock.Builder.addCode(columnRepresentation: String, defaultValue: CodeBlock? = null,
                                           index: Int = -1,
                                           modelBlock: CodeBlock = CodeBlock.of("model"),
                                           defineProperty: Boolean = true)

    open fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int = -1) {

    }
}

class SimpleAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(columnRepresentation: String,
                                           defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean) {
        statement("return \$L", getFieldAccessBlock(this, modelBlock))
    }

}

class ExistenceAccessCombiner(combiner: Combiner,
                              val autoRowId: Boolean,
                              val quickCheckPrimaryKey: Boolean,
                              val tableClassName: ClassName)
    : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(columnRepresentation: String,
                                           defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean) {

        combiner.apply {
            if (autoRowId) {
                val access = getFieldAccessBlock(this@addCode, modelBlock)

                add("return ")

                if (!fieldTypeName.isPrimitive) {
                    add("(\$L != null && ", access)
                }
                add("\$L > 0", access)

                if (!fieldTypeName.isPrimitive) {
                    add(" || \$L == null)", access)
                }
            }

            if (!autoRowId || !quickCheckPrimaryKey) {
                if (autoRowId) {
                    add("\n&& ")
                } else {
                    add("return ")
                }

                add("\$T.selectCountOf()\n.from(\$T.class)\n" +
                    ".where(getPrimaryConditionClause(\$L))\n" +
                    ".hasData(wrapper)",
                    ClassNames.SQLITE, tableClassName, modelBlock)
            }
            add(";\n")
        }
    }

}

class ContentValuesCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {

    override fun CodeBlock.Builder.addCode(columnRepresentation: String,
                                           defaultValue: CodeBlock?, index: Int,
                                           modelBlock: CodeBlock, defineProperty: Boolean) {
        combiner.apply {
            val fieldAccess: CodeBlock = getFieldAccessBlock(this@addCode, modelBlock)
            if (fieldTypeName.isPrimitive) {
                statement("values.put(\$1S, \$2L)", QueryBuilder.quote(columnRepresentation), fieldAccess)
            } else {
                if (defaultValue != null) {
                    val storedFieldAccess = fieldAccess
                    var subWrapperFieldAccess = storedFieldAccess
                    if (subWrapperAccessor != null) {
                        subWrapperFieldAccess = subWrapperAccessor.get(storedFieldAccess)
                    }
                    statement("values.put(\$S, \$L != null ? \$L : \$L)",
                        QueryBuilder.quote(columnRepresentation), storedFieldAccess, subWrapperFieldAccess, defaultValue)
                } else {
                    statement("values.put(\$S, \$L)",
                        QueryBuilder.quote(columnRepresentation), fieldAccess)
                }
            }
        }
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement("values.putNull(\$S)", QueryBuilder.quote(columnRepresentation))
    }
}

class SqliteStatementAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(columnRepresentation: String,
                                           defaultValue: CodeBlock?, index: Int,
                                           modelBlock: CodeBlock, defineProperty: Boolean) {
        combiner.apply {
            val fieldAccess: CodeBlock = getFieldAccessBlock(this@addCode, modelBlock,
                defineProperty = defineProperty)
            val wrapperMethod = SQLiteHelper[wrapperFieldTypeName ?: fieldTypeName].sqliteStatementWrapperMethod
            val statementMethod = SQLiteHelper[fieldTypeName].sqLiteStatementMethod

            var offset = "$index + $columnRepresentation"
            if (columnRepresentation.isNullOrEmpty()) {
                offset = "$index"
            }
            if (fieldTypeName.isPrimitive) {
                statement("statement.bind$statementMethod($offset, $fieldAccess)")
            } else {
                val subWrapperFieldAccess = subWrapperAccessor?.get(fieldAccess) ?: fieldAccess
                if (!defaultValue.toString().isNullOrEmpty()) {
                    `if`("$fieldAccess != null") {
                        statement("statement.bind$wrapperMethod($offset, $subWrapperFieldAccess)")
                    }.`else` {
                        statement("statement.bind$statementMethod($offset, $defaultValue)")
                    }
                } else {
                    statement("statement.bind${wrapperMethod}OrNull($offset, $subWrapperFieldAccess)")
                }
            }
        }
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        var access = "$index + $columnRepresentation"
        if (columnRepresentation.isEmpty()) {
            access = "$index"
        }
        code.addStatement("statement.bindNull($access)")
    }
}

class LoadFromCursorAccessCombiner(combiner: Combiner,
                                   val hasDefaultValue: Boolean,
                                   val nameAllocator: NameAllocator,
                                   val orderedCursorLookup: Boolean = false,
                                   val assignDefaultValuesFromCursor: Boolean = true)
    : ColumnAccessCombiner(combiner) {

    override fun CodeBlock.Builder.addCode(columnRepresentation: String,
                                           defaultValue: CodeBlock?, index: Int,
                                           modelBlock: CodeBlock, defineProperty: Boolean) {
        combiner.apply {
            var indexName = if (!orderedCursorLookup) {
                CodeBlock.of(columnRepresentation.S)
            } else {
                CodeBlock.of(index.toString())
            }!!

            if (wrapperLevelAccessor != null) {
                if (!orderedCursorLookup) {
                    indexName = CodeBlock.of(nameAllocator.newName("index_$columnRepresentation", columnRepresentation))
                    statement("\$T \$L = cursor.getColumnIndex(\$S)", Int::class.java, indexName,
                        columnRepresentation)
                    beginControlFlow("if (\$1L != -1 && !cursor.isNull(\$1L))", indexName)
                } else {
                    beginControlFlow("if (!cursor.isNull(\$1L))", index)
                }
                val cursorAccess = CodeBlock.of("cursor.\$L(\$L)",
                    SQLiteHelper.getMethod(wrapperFieldTypeName ?: fieldTypeName), indexName)
                // special case where we need to append try catch hack
                val isEnum = wrapperLevelAccessor is EnumColumnAccessor
                if (isEnum) {
                    beginControlFlow("try")
                }
                if (subWrapperAccessor != null) {
                    statement(fieldLevelAccessor.set(
                        wrapperLevelAccessor.set(subWrapperAccessor.set(cursorAccess)), modelBlock))
                } else {
                    statement(fieldLevelAccessor.set(
                        wrapperLevelAccessor.set(cursorAccess), modelBlock))
                }
                if (isEnum) {
                    catch(IllegalArgumentException::class) {
                        if (assignDefaultValuesFromCursor) {
                            statement(fieldLevelAccessor.set(wrapperLevelAccessor.set(defaultValue,
                                isDefault = true), modelBlock))
                        } else {
                            statement(fieldLevelAccessor.set(defaultValue, modelBlock))
                        }
                    }
                }
                if (assignDefaultValuesFromCursor) {
                    nextControlFlow("else")
                    statement(fieldLevelAccessor.set(wrapperLevelAccessor.set(defaultValue,
                        isDefault = true), modelBlock))
                }
                endControlFlow()
            } else {
                var hasDefault = hasDefaultValue
                var defaultValueBlock = defaultValue
                if (!assignDefaultValuesFromCursor) {
                    defaultValueBlock = fieldLevelAccessor.get(modelBlock)
                } else if (!hasDefault && fieldTypeName.isBoxedPrimitive) {
                    hasDefault = true // force a null on it.
                }
                val cursorAccess = CodeBlock.of("cursor.\$LOrDefault(\$L${if (hasDefault) ", $defaultValueBlock" else ""})",
                    SQLiteHelper.getMethod(wrapperFieldTypeName ?: fieldTypeName), indexName)
                statement(fieldLevelAccessor.set(cursorAccess, modelBlock))
            }
        }
    }
}

class PrimaryReferenceAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(columnRepresentation: String,
                                           defaultValue: CodeBlock?, index: Int,
                                           modelBlock: CodeBlock, defineProperty: Boolean) {
        val wrapperLevelAccessor = this@PrimaryReferenceAccessCombiner.combiner.wrapperLevelAccessor
        statement("clause.and(\$L.\$Leq(\$L))", columnRepresentation,
            if (!wrapperLevelAccessor.isPrimitiveTarget()) "invertProperty()." else "",
            getFieldAccessBlock(this, modelBlock, wrapperLevelAccessor !is BooleanColumnAccessor))
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement("clause.and(\$L.eq((\$T) \$L))", columnRepresentation,
            ClassNames.ICONDITIONAL, "null")
    }
}

class UpdateAutoIncrementAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(columnRepresentation: String, defaultValue: CodeBlock?,
                                           index: Int, modelBlock: CodeBlock, defineProperty: Boolean) {
        combiner.apply {
            var method = ""
            if (SQLiteHelper.containsNumberMethod(fieldTypeName.unbox())) {
                method = fieldTypeName.unbox().toString()
            }

            statement(fieldLevelAccessor.set(CodeBlock.of("id.\$LValue()", method), modelBlock))
        }
    }

}

class CachingIdAccessCombiner(combiner: Combiner)
    : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(columnRepresentation: String,
                                           defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean) {
        statement("inValues[\$L] = \$L", index, getFieldAccessBlock(this, modelBlock))
    }

}

class SaveModelAccessCombiner(combiner: Combiner,
                              val implementsModel: Boolean,
                              val extendsBaseModel: Boolean)
    : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(columnRepresentation: String,
                                           defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean) {
        combiner.apply {
            val access = getFieldAccessBlock(this@addCode, modelBlock)
            `if`("$access != null") {
                if (implementsModel) {
                    statement("$access.save(${wrapperIfBaseModel(extendsBaseModel)})")
                } else {
                    statement("\$T.getModelAdapter(\$T.class).save($access, ${ModelUtils.wrapper})",
                        ClassNames.FLOW_MANAGER, fieldTypeName)
                }
            }.end()
        }
    }

}

class DeleteModelAccessCombiner(combiner: Combiner,
                                val implementsModel: Boolean,
                                val extendsBaseModel: Boolean)
    : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(columnRepresentation: String,
                                           defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean) {
        combiner.apply {
            val access = getFieldAccessBlock(this@addCode, modelBlock)
            `if`("$access != null") {
                if (implementsModel) {
                    statement("$access.delete(${wrapperIfBaseModel(extendsBaseModel)})")
                } else {
                    statement("\$T.getModelAdapter(\$T.class).delete($access, ${ModelUtils.wrapper})",
                        ClassNames.FLOW_MANAGER, fieldTypeName)
                }
            }.end()
        }
    }

}

fun wrapperIfBaseModel(extendsBaseModel: Boolean) = if (extendsBaseModel) ModelUtils.wrapper else ""
fun wrapperCommaIfBaseModel(extendsBaseModel: Boolean) = if (extendsBaseModel) ", " + ModelUtils.wrapper else ""