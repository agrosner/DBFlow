package com.dbflow5.processor.definition.column

import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.SQLiteHelper
import com.dbflow5.processor.definition.behavior.CursorHandlingBehavior
import com.dbflow5.processor.utils.ModelUtils
import com.dbflow5.processor.utils.catch
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.processor.utils.statement
import com.dbflow5.quote
import com.grosner.kpoet.S
import com.grosner.kpoet.`else`
import com.grosner.kpoet.`if`
import com.grosner.kpoet.end
import com.grosner.kpoet.statement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.TypeName

data class Combiner(
    val fieldLevelAccessor: ColumnAccessor,
    val fieldTypeName: TypeName,
    val wrapperLevelAccessor: ColumnAccessor? = null,
    val wrapperFieldTypeName: TypeName? = null,
    val subWrapperAccessor: ColumnAccessor? = null,
    val customPrefixName: String = ""
)

abstract class ColumnAccessCombiner(val combiner: Combiner) {

    private val nameAllocator = NameAllocator()

    fun getFieldAccessBlock(
        existingBuilder: CodeBlock.Builder,
        modelBlock: CodeBlock,
        useWrapper: Boolean = true,
        defineProperty: Boolean = true
    ): CodeBlock {
        var fieldAccess: CodeBlock
        combiner.apply {
            if (wrapperLevelAccessor != null && !fieldTypeName.isPrimitive) {
                fieldAccess =
                    CodeBlock.of("${nameAllocator.newName(customPrefixName)}ref" + fieldLevelAccessor.propertyName)

                if (defineProperty) {
                    val fieldAccessorBlock = fieldLevelAccessor.get(modelBlock)
                    val wrapperAccessorBlock = wrapperLevelAccessor.get(fieldAccessorBlock)
                    // if same, don't extra null check.
                    if (fieldLevelAccessor.toString() != wrapperLevelAccessor.toString()
                        && wrapperLevelAccessor !is TypeConverterScopeColumnAccessor
                    ) {
                        existingBuilder.addStatement(
                            "\$T \$L = \$L != null ? \$L : null",
                            wrapperFieldTypeName,
                            fieldAccess,
                            fieldAccessorBlock,
                            wrapperAccessorBlock
                        )
                    } else if (wrapperLevelAccessor is TypeConverterScopeColumnAccessor) {
                        existingBuilder.addStatement(
                            "\$T \$L = \$L", wrapperFieldTypeName,
                            fieldAccess, wrapperAccessorBlock
                        )
                    } else {
                        existingBuilder.addStatement(
                            "\$T \$L = \$L", wrapperFieldTypeName,
                            fieldAccess, fieldAccessorBlock
                        )
                    }
                }
            } else {
                fieldAccess = if (useWrapper && wrapperLevelAccessor != null) {
                    wrapperLevelAccessor.get(fieldLevelAccessor.get(modelBlock))
                } else {
                    fieldLevelAccessor.get(modelBlock)
                }
            }
        }
        return fieldAccess
    }

    abstract fun CodeBlock.Builder.addCode(
        columnRepresentation: String, defaultValue: CodeBlock? = null,
        index: Int = -1,
        modelBlock: CodeBlock = CodeBlock.of("model"),
        defineProperty: Boolean = true
    )

    open fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int = -1) {

    }
}

class SimpleAccessCombiner(combiner: Combiner) : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String,
        defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean
    ) {
        statement("return \$L", getFieldAccessBlock(this, modelBlock))
    }

}

class ExistenceAccessCombiner(
    combiner: Combiner,
    val autoRowId: Boolean,
    val quickCheckPrimaryKey: Boolean,
    val tableClassName: ClassName
) : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String,
        defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean
    ) {

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

                add(
                    "\$T.selectCountOf()\n.from(\$T.getKotlinClass(\$T.class))\n" +
                        ".where(getPrimaryConditionClause(\$L))\n" +
                        ".hasData(wrapper)",
                    ClassNames.SQLITE,
                    ClassNames.JVM_CLASS_MAPPING,
                    tableClassName, modelBlock
                )
            }
            add(";\n")
        }
    }

}

class ContentValuesCombiner(combiner: Combiner) : ColumnAccessCombiner(combiner) {

    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String,
        defaultValue: CodeBlock?, index: Int,
        modelBlock: CodeBlock, defineProperty: Boolean
    ) {
        combiner.apply {
            val fieldAccess: CodeBlock = getFieldAccessBlock(this@addCode, modelBlock)
            if (fieldTypeName.isPrimitive) {
                statement("values.put(\$1S, \$2L)", columnRepresentation.quote(), fieldAccess)
            } else {
                if (defaultValue != null) {
                    var subWrapperFieldAccess = fieldAccess
                    if (subWrapperAccessor != null) {
                        subWrapperFieldAccess = subWrapperAccessor.get(fieldAccess)
                    }
                    if (fieldAccess.toString() != subWrapperFieldAccess.toString()
                        || defaultValue.toString() != "null"
                    ) {
                        statement(
                            "values.put(\$S, \$L != null ? \$L : \$L)",
                            columnRepresentation.quote(),
                            fieldAccess,
                            subWrapperFieldAccess,
                            defaultValue
                        )
                    } else {
                        // if same default value is null and object reference is same as subwrapper.
                        statement(
                            "values.put(\$S, \$L)",
                            columnRepresentation.quote(), fieldAccess
                        )
                    }
                } else {
                    statement(
                        "values.put(\$S, \$L)",
                        columnRepresentation.quote(), fieldAccess
                    )
                }
            }
        }
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement("values.putNull(\$S)", columnRepresentation.quote())
    }
}

class SqliteStatementAccessCombiner(combiner: Combiner) : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String,
        defaultValue: CodeBlock?, index: Int,
        modelBlock: CodeBlock, defineProperty: Boolean
    ) {
        combiner.apply {
            val fieldAccess: CodeBlock = getFieldAccessBlock(
                this@addCode, modelBlock,
                defineProperty = defineProperty
            )
            val wrapperMethod = SQLiteHelper.getWrapperMethod(wrapperFieldTypeName ?: fieldTypeName)
            val statementMethod =
                SQLiteHelper[wrapperFieldTypeName ?: fieldTypeName].sqLiteStatementMethod

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
                    if (subWrapperAccessor != null) {
                        statement("statement.bind${wrapperMethod}OrNull($offset, $fieldAccess != null ? $subWrapperFieldAccess : null)")
                    } else {
                        statement("statement.bind${wrapperMethod}OrNull($offset, $subWrapperFieldAccess)")
                    }

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

class LoadFromCursorAccessCombiner(
    combiner: Combiner,
    val hasDefaultValue: Boolean,
    val nameAllocator: NameAllocator,
    val cursorHandlingBehavior: CursorHandlingBehavior
) : ColumnAccessCombiner(combiner) {

    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String,
        defaultValue: CodeBlock?, index: Int,
        modelBlock: CodeBlock, defineProperty: Boolean
    ) {
        combiner.apply {
            var indexName = if (!cursorHandlingBehavior.orderedCursorLookup) {
                CodeBlock.of(columnRepresentation.S)
            } else {
                CodeBlock.of(index.toString())
            }!!

            if (wrapperLevelAccessor != null) {
                if (!cursorHandlingBehavior.orderedCursorLookup) {
                    indexName = CodeBlock.of(
                        nameAllocator.newName(
                            "index_$columnRepresentation",
                            columnRepresentation
                        )
                    )
                    statement(
                        "\$T \$L = cursor.getColumnIndex(\$S)", Int::class.java, indexName,
                        columnRepresentation
                    )
                    beginControlFlow("if (\$1L != -1 && !cursor.isNull(\$1L))", indexName)
                } else {
                    beginControlFlow("if (!cursor.isNull(\$1L))", index)
                }
                val cursorAccess = CodeBlock.of(
                    "cursor.\$L(\$L)",
                    SQLiteHelper.getMethod(wrapperFieldTypeName ?: fieldTypeName), indexName
                )
                // special case where we need to append try catch hack
                val isEnum = wrapperLevelAccessor is EnumColumnAccessor
                if (isEnum) {
                    beginControlFlow("try")
                }
                if (subWrapperAccessor != null) {
                    statement(
                        fieldLevelAccessor.set(
                            wrapperLevelAccessor.set(subWrapperAccessor.set(cursorAccess)),
                            modelBlock
                        )
                    )
                } else {
                    statement(
                        fieldLevelAccessor.set(
                            wrapperLevelAccessor.set(cursorAccess), modelBlock
                        )
                    )
                }
                if (isEnum) {
                    catch(IllegalArgumentException::class) {
                        if (cursorHandlingBehavior.assignDefaultValuesFromCursor) {
                            statement(
                                fieldLevelAccessor.set(
                                    wrapperLevelAccessor.set(
                                        defaultValue,
                                        isDefault = true
                                    ), modelBlock
                                )
                            )
                        } else {
                            statement(fieldLevelAccessor.set(defaultValue, modelBlock))
                        }
                    }
                }
                if (cursorHandlingBehavior.assignDefaultValuesFromCursor) {
                    nextControlFlow("else")
                    statement(
                        fieldLevelAccessor.set(
                            wrapperLevelAccessor.set(
                                defaultValue,
                                isDefault = true
                            ), modelBlock
                        )
                    )
                }
                endControlFlow()
            } else {
                var hasDefault = hasDefaultValue
                var defaultValueBlock = defaultValue
                if (!cursorHandlingBehavior.assignDefaultValuesFromCursor) {
                    defaultValueBlock = fieldLevelAccessor.get(modelBlock)
                } else if (!hasDefault && fieldTypeName.isBoxedPrimitive) {
                    hasDefault = true // force a null on it.
                }
                val cursorAccess = CodeBlock.of(
                    "cursor.\$LOrDefault(\$L${if (hasDefault) ", $defaultValueBlock" else ""})",
                    SQLiteHelper.getMethod(wrapperFieldTypeName ?: fieldTypeName), indexName
                )
                statement(fieldLevelAccessor.set(cursorAccess, modelBlock))
            }
        }
    }
}

class PrimaryReferenceAccessCombiner(combiner: Combiner) : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String,
        defaultValue: CodeBlock?, index: Int,
        modelBlock: CodeBlock, defineProperty: Boolean
    ) {
        val wrapperLevelAccessor = this@PrimaryReferenceAccessCombiner.combiner.wrapperLevelAccessor
        statement(
            "clause.and(\$L.\$Leq(\$L))", columnRepresentation,
            if (!wrapperLevelAccessor.isPrimitiveTarget()) "invertProperty()." else "",
            getFieldAccessBlock(this, modelBlock, wrapperLevelAccessor !is BooleanColumnAccessor)
        )
    }

    override fun addNull(code: CodeBlock.Builder, columnRepresentation: String, index: Int) {
        code.addStatement(
            "clause.and(\$L.eq((\$T) \$L))", columnRepresentation,
            ClassNames.ICONDITIONAL, "null"
        )
    }
}

class UpdateAutoIncrementAccessCombiner(combiner: Combiner) : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String, defaultValue: CodeBlock?,
        index: Int, modelBlock: CodeBlock, defineProperty: Boolean
    ) {
        combiner.apply {
            var method = ""
            if (SQLiteHelper.containsNumberMethod(fieldTypeName.unbox())) {
                method = fieldTypeName.unbox().toString()
            }

            statement(fieldLevelAccessor.set(CodeBlock.of("id.\$LValue()", method), modelBlock))
        }
    }

}

class CachingIdAccessCombiner(combiner: Combiner) : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String,
        defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean
    ) {
        statement("inValues[\$L] = \$L", index, getFieldAccessBlock(this, modelBlock))
    }

}

class SaveModelAccessCombiner(combiner: Combiner) : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String,
        defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean
    ) {
        combiner.apply {
            val access = getFieldAccessBlock(this@addCode, modelBlock)
            `if`("$access != null") {
                statement(
                    "\$T.getModelAdapter(\$T.getKotlinClass(\$T.class))" +
                        ".jvmSave($access, ${ModelUtils.wrapper})",
                    ClassNames.FLOW_MANAGER,
                    ClassNames.JVM_CLASS_MAPPING,
                    fieldTypeName
                )
            }.end()
        }
    }

}

class DeleteModelAccessCombiner(
    combiner: Combiner,
) : ColumnAccessCombiner(combiner) {
    override fun CodeBlock.Builder.addCode(
        columnRepresentation: String,
        defaultValue: CodeBlock?, index: Int, modelBlock: CodeBlock, defineProperty: Boolean
    ) {
        combiner.apply {
            val access = getFieldAccessBlock(this@addCode, modelBlock)
            `if`("$access != null") {
                statement(
                    "\$T.getModelAdapter(\$T.getKotlinClass(\$T.class))" +
                        ".delete($access, ${ModelUtils.wrapper})",
                    ClassNames.FLOW_MANAGER,
                    ClassNames.JVM_CLASS_MAPPING,
                    fieldTypeName
                )
            }.end()
        }
    }

}

fun wrapperIfBaseModel(extendsBaseModel: Boolean) = if (extendsBaseModel) ModelUtils.wrapper else ""
fun wrapperCommaIfBaseModel(extendsBaseModel: Boolean) =
    if (extendsBaseModel) ", " + ModelUtils.wrapper else ""