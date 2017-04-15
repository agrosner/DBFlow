package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.lang.model.element.Modifier

/**
 * Description:
 */
interface MethodDefinition {

    val methodSpec: MethodSpec?
}

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */
/**
 * Description: Writes the bind to content values method in the ModelDAO.
 */
class BindToContentValuesMethod(private val baseTableDefinition: BaseTableDefinition,
                                private val isInsert: Boolean,
                                private val implementsContentValuesListener: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val methodBuilder = MethodSpec.methodBuilder(if (isInsert) "bindToInsertValues" else "bindToContentValues")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.CONTENT_VALUES, PARAM_CONTENT_VALUES)
                    .addParameter(baseTableDefinition.parameterClassName, ModelUtils.variable)
                    .returns(TypeName.VOID)

            var retMethodBuilder: MethodSpec.Builder? = methodBuilder

            if (isInsert) {
                baseTableDefinition.columnDefinitions.forEach {
                    if (!it.isPrimaryKeyAutoIncrement && !it.isRowId) {
                        methodBuilder.addCode(it.contentValuesStatement)
                    }
                }

                if (implementsContentValuesListener) {
                    methodBuilder.addStatement("\$L.onBindTo\$LValues(\$L)",
                            ModelUtils.variable, if (isInsert) "Insert" else "Content", PARAM_CONTENT_VALUES)
                }
            } else {
                if (baseTableDefinition.hasAutoIncrement || baseTableDefinition.hasRowID) {
                    val autoIncrement = baseTableDefinition.autoIncrementColumn
                    autoIncrement?.let {
                        methodBuilder.addCode(autoIncrement.contentValuesStatement)
                    }
                } else if (!implementsContentValuesListener) {
                    retMethodBuilder = null
                }

                methodBuilder.addStatement("bindToInsertValues(\$L, \$L)", PARAM_CONTENT_VALUES, ModelUtils.variable)
                if (implementsContentValuesListener) {
                    methodBuilder.addStatement("\$L.onBindTo\$LValues(\$L)",
                            ModelUtils.variable, if (isInsert) "Insert" else "Content", PARAM_CONTENT_VALUES)
                }
            }

            return retMethodBuilder?.build()
        }

    companion object {
        val PARAM_CONTENT_VALUES = "values"
    }
}

/**
 * Description:
 */
class BindToStatementMethod(private val tableDefinition: TableDefinition, private val isInsert: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val methodBuilder = MethodSpec.methodBuilder(if (isInsert) "bindToInsertStatement" else "bindToStatement")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.DATABASE_STATEMENT, PARAM_STATEMENT)
                    .addParameter(tableDefinition.parameterClassName,
                            ModelUtils.variable).returns(TypeName.VOID)

            // write the reference method
            if (isInsert) {
                methodBuilder.addParameter(TypeName.INT, PARAM_START)
                val realCount = AtomicInteger(1)
                tableDefinition.columnDefinitions.forEach {
                    if (!it.isPrimaryKeyAutoIncrement && !it.isRowId) {
                        methodBuilder.addCode(it.getSQLiteStatementMethod(realCount))
                        realCount.incrementAndGet()
                    }
                }

                if (tableDefinition.implementsSqlStatementListener) {
                    methodBuilder.addStatement("\$L.onBindTo\$LStatement(\$L)",
                            ModelUtils.variable, if (isInsert) "Insert" else "", PARAM_STATEMENT)
                }
            } else {
                var start = 0
                if (tableDefinition.hasAutoIncrement || tableDefinition.hasRowID) {
                    val autoIncrement = tableDefinition.autoIncrementColumn
                    autoIncrement?.let {
                        methodBuilder.addStatement("int start = 0")
                        methodBuilder.addCode(it.getSQLiteStatementMethod(AtomicInteger(++start)))
                    }
                    methodBuilder.addStatement("bindToInsertStatement(\$L, \$L, \$L)", PARAM_STATEMENT, ModelUtils.variable, start)
                } else if (tableDefinition.implementsSqlStatementListener) {
                    methodBuilder.addStatement("bindToInsertStatement(\$L, \$L, \$L)", PARAM_STATEMENT, ModelUtils.variable, start)
                    methodBuilder.addStatement("\$L.onBindTo\$LStatement(\$L)",
                            ModelUtils.variable, if (isInsert) "Insert" else "", PARAM_STATEMENT)
                } else {
                    // don't generate method
                    return null
                }
            }

            return methodBuilder.build()
        }

    companion object {

        val PARAM_STATEMENT = "statement"
        val PARAM_START = "start"
    }
}

/**
 * Description:
 */
class CreationQueryMethod(private val tableDefinition: TableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() {
            val methodBuilder = MethodSpec.methodBuilder("getCreationQuery")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(ClassName.get(String::class.java))

            val creationBuilder = CodeBlock.builder().add("CREATE TABLE IF NOT EXISTS ")
                    .add(QueryBuilder.quote(tableDefinition.tableName)).add("(")

            (0..tableDefinition.columnDefinitions.size - 1).forEach { i ->
                if (i > 0) {
                    creationBuilder.add(",")
                }
                creationBuilder.add(tableDefinition.columnDefinitions[i].creationName)
            }

            tableDefinition.uniqueGroupsDefinitions.forEach {
                if (!it.columnDefinitionList.isEmpty()) creationBuilder.add(it.creationName)
            }

            if (!tableDefinition.hasAutoIncrement) {
                val primarySize = tableDefinition.primaryColumnDefinitions.size
                for (i in 0..primarySize - 1) {
                    if (i == 0) {
                        creationBuilder.add(", PRIMARY KEY(")
                    }

                    if (i > 0) {
                        creationBuilder.add(",")
                    }

                    val primaryDefinition = tableDefinition.primaryColumnDefinitions[i]
                    creationBuilder.add(primaryDefinition.primaryKeyName)

                    if (i == primarySize - 1) {
                        creationBuilder.add(")")
                        if (!tableDefinition.primaryKeyConflictActionName.isNullOrEmpty()) {
                            creationBuilder.add(" ON CONFLICT " + tableDefinition.primaryKeyConflictActionName)
                        }
                    }
                }
            }

            val foreignSize = tableDefinition.foreignKeyDefinitions.size

            val foreignKeyBlocks = ArrayList<CodeBlock>()
            val tableNameBlocks = ArrayList<CodeBlock>()
            val referenceKeyBlocks = ArrayList<CodeBlock>()

            for (i in 0..foreignSize - 1) {
                val foreignKeyBuilder = CodeBlock.builder()
                val referenceBuilder = CodeBlock.builder()
                val foreignKeyColumnDefinition = tableDefinition.foreignKeyDefinitions[i]

                foreignKeyBuilder.add(", FOREIGN KEY(")

                (0..foreignKeyColumnDefinition._foreignKeyReferenceDefinitionList.size - 1).forEach { j ->
                    if (j > 0) {
                        foreignKeyBuilder.add(",")
                    }
                    val referenceDefinition = foreignKeyColumnDefinition._foreignKeyReferenceDefinitionList[j]
                    foreignKeyBuilder.add("\$L", QueryBuilder.quote(referenceDefinition.columnName))
                }


                foreignKeyBuilder.add(") REFERENCES ")

                foreignKeyBlocks.add(foreignKeyBuilder.build())

                tableNameBlocks.add(CodeBlock.builder().add("\$T.getTableName(\$T.class)",
                        ClassNames.FLOW_MANAGER, foreignKeyColumnDefinition.referencedTableClassName).build())

                referenceBuilder.add("(")
                for (j in 0..foreignKeyColumnDefinition._foreignKeyReferenceDefinitionList.size - 1) {
                    if (j > 0) {
                        referenceBuilder.add(", ")
                    }
                    val referenceDefinition = foreignKeyColumnDefinition._foreignKeyReferenceDefinitionList[j]
                    referenceBuilder.add("\$L", QueryBuilder.quote(referenceDefinition.foreignColumnName))
                }
                referenceBuilder.add(") ON UPDATE \$L ON DELETE \$L", foreignKeyColumnDefinition.onUpdate.name.replace("_", " "),
                        foreignKeyColumnDefinition.onDelete.name.replace("_", " "))
                referenceKeyBlocks.add(referenceBuilder.build())
            }

            val codeBuilder = CodeBlock.builder().add("return \$S", creationBuilder.build().toString())

            if (foreignSize > 0) {
                for (i in 0..foreignSize - 1) {
                    codeBuilder.add("+ \$S + \$L + \$S", foreignKeyBlocks[i], tableNameBlocks[i], referenceKeyBlocks[i])
                }
            }
            codeBuilder.add(" + \$S", ");").add(";\n")


            methodBuilder.addCode(codeBuilder.build())

            return methodBuilder.build()
        }
}

/**
 * Description: Writes out the custom type converter fields.
 */
class CustomTypeConverterPropertyMethod(private val baseTableDefinition: BaseTableDefinition)
    : TypeAdder, CodeAdder {


    override fun addToType(typeBuilder: TypeSpec.Builder) {
        val customTypeConverters = baseTableDefinition.associatedTypeConverters.keys
        customTypeConverters.forEach {
            typeBuilder.addField(FieldSpec.builder(it, "typeConverter" + it.simpleName(),
                    Modifier.PRIVATE, Modifier.FINAL).initializer("new \$T()", it).build())
        }

        val globalTypeConverters = baseTableDefinition.globalTypeConverters.keys
        globalTypeConverters.forEach {
            typeBuilder.addField(FieldSpec.builder(it, "global_typeConverter" + it.simpleName(),
                    Modifier.PRIVATE, Modifier.FINAL).build())
        }


    }

    override fun addCode(code: CodeBlock.Builder): CodeBlock.Builder {
        // Constructor code
        val globalTypeConverters = baseTableDefinition.globalTypeConverters.keys
        globalTypeConverters.forEach {
            val def = baseTableDefinition.globalTypeConverters[it]
            val firstDef = def?.get(0)
            firstDef?.typeConverterElementNames?.forEach { elementName ->
                code.addStatement("global_typeConverter\$L = (\$T) \$L.getTypeConverterForClass(\$T.class)",
                        it.simpleName(), it, "holder", elementName).build()
            }
        }
        return code
    }
}

/**
 * Description:
 */
class ExistenceMethod(private val tableDefinition: BaseTableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() {
            val methodBuilder = MethodSpec.methodBuilder("exists")
                    .addAnnotation(Override::class.java)
                    .addParameter(tableDefinition.parameterClassName, ModelUtils.variable)
                    .addParameter(ClassNames.DATABASE_WRAPPER, "wrapper")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL).returns(TypeName.BOOLEAN)
            // only quick check if enabled.
            var primaryColumn = tableDefinition.autoIncrementColumn
            if (primaryColumn == null) {
                primaryColumn = tableDefinition.primaryColumnDefinitions[0]
            }

            val code = CodeBlock.builder()
            primaryColumn.appendExistenceMethod(code)
            methodBuilder.addCode(code.build())
            return methodBuilder.build()
        }
}

/**
 * Description:
 */
class InsertStatementQueryMethod(private val tableDefinition: TableDefinition, private val isInsert: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            if (isInsert && !tableDefinition.hasAutoIncrement) {
                return null // dont write method here because we reuse the compiled statement query method
            }
            val methodBuilder = MethodSpec.methodBuilder(if (isInsert) "getInsertStatementQuery" else "getCompiledStatementQuery")
                    .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(ClassName.get(String::class.java))

            val codeBuilder = CodeBlock.builder().add("INSERT ")
            if (!tableDefinition.insertConflictActionName.isEmpty()) {
                codeBuilder.add("OR \$L ", tableDefinition.insertConflictActionName)
            }
            codeBuilder.add("INTO ").add(QueryBuilder.quote(tableDefinition.tableName))

            val isSingleAutoincrement = tableDefinition.hasAutoIncrement && tableDefinition.columnDefinitions.size == 1
                    && isInsert

            codeBuilder.add("(")

            val columnSize = tableDefinition.columnDefinitions.size
            var columnCount = 0
            tableDefinition.columnDefinitions.forEach {
                if (!it.isPrimaryKeyAutoIncrement && !it.isRowId || !isInsert || isSingleAutoincrement) {
                    if (columnCount > 0) codeBuilder.add(",")

                    codeBuilder.add(it.insertStatementColumnName)
                    columnCount++
                }
            }
            codeBuilder.add(")")

            codeBuilder.add(" VALUES (")

            columnCount = 0
            for (i in 0..columnSize - 1) {
                val definition = tableDefinition.columnDefinitions[i]
                if (!definition.isPrimaryKeyAutoIncrement && !definition.isRowId || !isInsert) {
                    if (columnCount > 0) {
                        codeBuilder.add(",")
                    }

                    codeBuilder.add(definition.insertStatementValuesString)
                    columnCount++
                }
            }

            if (isSingleAutoincrement) {
                codeBuilder.add("NULL")
            }

            codeBuilder.add(")")

            methodBuilder.addStatement("return \$S", codeBuilder.build().toString())

            return methodBuilder.build()
        }
}

/**
 * Description:
 */
class LoadFromCursorMethod(private val baseTableDefinition: BaseTableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() {
            val methodBuilder = MethodSpec.methodBuilder("loadFromCursor")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.FLOW_CURSOR, PARAM_CURSOR)
                    .addParameter(baseTableDefinition.parameterClassName,
                            ModelUtils.variable).returns(TypeName.VOID)

            val index = AtomicInteger(0)
            baseTableDefinition.columnDefinitions.forEach {
                methodBuilder.addCode(it.getLoadFromCursorMethod(true, index))
                index.incrementAndGet()
            }

            if (baseTableDefinition is TableDefinition) {

                val codeBuilder = CodeBlock.builder()
                baseTableDefinition.oneToManyDefinitions
                        .filter { it.isLoad }
                        .forEach { it.writeLoad(codeBuilder) }
                methodBuilder.addCode(codeBuilder.build())
            }

            if (baseTableDefinition is TableDefinition && baseTableDefinition.implementsLoadFromCursorListener) {
                methodBuilder.addStatement("\$L.onLoadFromCursor(\$L)", ModelUtils.variable, PARAM_CURSOR)
            }

            return methodBuilder.build()
        }

    companion object {

        val PARAM_CURSOR = "cursor"
    }
}

/**
 * Description:
 */
class OneToManyDeleteMethod(private val tableDefinition: TableDefinition,
                            private val useWrapper: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            var shouldWrite = false
            for (oneToManyDefinition in tableDefinition.oneToManyDefinitions) {
                if (oneToManyDefinition.isDelete) {
                    shouldWrite = true
                    break
                }
            }

            if (shouldWrite || tableDefinition.cachingEnabled) {

                val builder = CodeBlock.builder()

                if (tableDefinition.cachingEnabled) {
                    builder.addStatement("getModelCache().removeModel(getCachingId(\$L))", ModelUtils.variable)
                }

                builder.addStatement("boolean successful = super.delete(\$L\$L)", ModelUtils.variable,
                        if (useWrapper) ", " + ModelUtils.wrapper else "")

                tableDefinition.oneToManyDefinitions.forEach { it.writeDelete(builder, useWrapper) }

                builder.addStatement("return successful")

                val delete = MethodSpec.methodBuilder("delete")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addParameter(tableDefinition.elementClassName, ModelUtils.variable)
                        .addCode(builder.build())
                        .returns(TypeName.BOOLEAN)
                if (useWrapper) {
                    delete.addParameter(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)
                }
                return delete.build()
            }
            return null
        }
}

/**
 * Description: Overrides the save, update, and insert methods if the [com.raizlabs.android.dbflow.annotation.OneToMany.Method.SAVE] is used.
 */
class OneToManySaveMethod(private val tableDefinition: TableDefinition,
                          private val methodName: String,
                          private val useWrapper: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            if (!tableDefinition.oneToManyDefinitions.isEmpty() || tableDefinition.cachingEnabled) {
                val code = CodeBlock.builder()

                if (methodName == METHOD_INSERT) {
                    code.add("long rowId = ")
                } else if (methodName == METHOD_UPDATE || methodName == METHOD_SAVE) {
                    code.add("boolean successful = ")
                }

                code.addStatement("super.\$L(\$L\$L)", methodName,
                        ModelUtils.variable,
                        if (useWrapper) ", " + ModelUtils.wrapper else "")

                if (tableDefinition.cachingEnabled) {
                    code.addStatement("getModelCache().addModel(getCachingId(\$L), \$L)", ModelUtils.variable,
                            ModelUtils.variable)
                }

                for (oneToManyDefinition in tableDefinition.oneToManyDefinitions) {
                    when (methodName) {
                        METHOD_SAVE -> oneToManyDefinition.writeSave(code, useWrapper)
                        METHOD_UPDATE -> oneToManyDefinition.writeUpdate(code, useWrapper)
                        METHOD_INSERT -> oneToManyDefinition.writeInsert(code, useWrapper)
                    }
                }

                val builder = MethodSpec.methodBuilder(methodName)
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addParameter(tableDefinition.elementClassName, ModelUtils.variable)
                        .addCode(code.build())
                if (methodName == METHOD_INSERT) {
                    builder.returns(ClassName.LONG)
                    builder.addStatement("return rowId")
                } else if (methodName == METHOD_UPDATE || methodName == METHOD_SAVE) {
                    builder.returns(TypeName.BOOLEAN)
                    builder.addStatement("return successful")
                }
                if (useWrapper) {
                    builder.addParameter(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)
                }

                return builder.build()
            } else {
                return null
            }
        }

    companion object {
        val METHOD_SAVE = "save"
        val METHOD_UPDATE = "update"
        val METHOD_INSERT = "insert"
    }
}

/**
 * Description: Creates a method that builds a clause of ConditionGroup that represents its primary keys. Useful
 * for updates or deletes.
 */
class PrimaryConditionMethod(private val tableDefinition: BaseTableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val methodBuilder = MethodSpec.methodBuilder("getPrimaryConditionClause")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(tableDefinition.parameterClassName,
                            ModelUtils.variable).returns(ClassNames.OPERATOR_GROUP)
            val code = CodeBlock.builder()
            code.addStatement("\$T clause = \$T.clause()", ClassNames.OPERATOR_GROUP, ClassNames.OPERATOR_GROUP)
            tableDefinition.primaryColumnDefinitions.forEach {
                val codeBuilder = CodeBlock.builder()
                it.appendPropertyComparisonAccessStatement(codeBuilder)
                code.add(codeBuilder.build())
            }
            methodBuilder.addCode(code.build())
            methodBuilder.addStatement("return clause")
            return methodBuilder.build()
        }
}
