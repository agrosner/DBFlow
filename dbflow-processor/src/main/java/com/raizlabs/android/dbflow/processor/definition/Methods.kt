package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.column.wrapperCommaIfBaseModel
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.`override fun`
import com.raizlabs.android.dbflow.processor.utils.codeBlock
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
                    methodBuilder.addStatement("${ModelUtils.variable}.onBindTo\$LStatement($PARAM_STATEMENT)",
                        if (isInsert) "Insert" else "")
                }
            } else {
                var start = 0
                if (tableDefinition.hasAutoIncrement || tableDefinition.hasRowID) {
                    val autoIncrement = tableDefinition.autoIncrementColumn
                    autoIncrement?.let {
                        methodBuilder.addStatement("int start = 0")
                        methodBuilder.addCode(it.getSQLiteStatementMethod(AtomicInteger(++start)))
                    }
                    methodBuilder.addStatement("bindToInsertStatement($PARAM_STATEMENT, ${ModelUtils.variable}, $start)")
                } else if (tableDefinition.implementsSqlStatementListener) {
                    methodBuilder.addStatement("bindToInsertStatement($PARAM_STATEMENT, ${ModelUtils.variable}, $start)")
                    methodBuilder.addStatement("${ModelUtils.variable}.onBindTo\$LStatement($PARAM_STATEMENT)",
                        if (isInsert) "Insert" else "")
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
        get() = `override fun`(String::class, "getCreationQuery") {
            modifiers(public, final)

            val foreignSize = tableDefinition.foreignKeyDefinitions.size

            val creationBuilder = codeBlock {
                add("CREATE TABLE IF NOT EXISTS ${QueryBuilder.quote(tableDefinition.tableName)}(")
                add(tableDefinition.columnDefinitions.joinToString { it.creationName.toString() })
                tableDefinition.uniqueGroupsDefinitions.forEach {
                    if (!it.columnDefinitionList.isEmpty()) add(it.creationName)
                }

                if (!tableDefinition.hasAutoIncrement) {
                    val primarySize = tableDefinition.primaryColumnDefinitions.size
                    if (primarySize > 0) {
                        add(", PRIMARY KEY(${tableDefinition.primaryColumnDefinitions.joinToString { it.primaryKeyName.toString() }})")
                        if (!tableDefinition.primaryKeyConflictActionName.isNullOrEmpty()) {
                            add(" ON CONFLICT ${tableDefinition.primaryKeyConflictActionName}")
                        }
                    }
                }
                if (foreignSize == 0) {
                    add(")")
                }
                this
            }

            val codeBuilder = CodeBlock.builder()
                .add("return ${creationBuilder.toString().S}")

            val foreignKeyBlocks = ArrayList<CodeBlock>()
            val tableNameBlocks = ArrayList<CodeBlock>()
            val referenceKeyBlocks = ArrayList<CodeBlock>()

            for (i in 0..foreignSize - 1) {
                val foreignKeyBuilder = CodeBlock.builder()
                val referenceBuilder = CodeBlock.builder()
                val fk = tableDefinition.foreignKeyDefinitions[i]

                foreignKeyBlocks.add(foreignKeyBuilder.apply {
                    add(", FOREIGN KEY(")
                    add(fk._foreignKeyReferenceDefinitionList.joinToString { QueryBuilder.quote(it.columnName) })
                    add(") REFERENCES ")
                }.build())

                tableNameBlocks.add(codeBlock { add("\$T.getTableName(\$T.class)", ClassNames.FLOW_MANAGER, fk.referencedTableClassName) })

                referenceKeyBlocks.add(referenceBuilder.apply {
                    add("(")
                    add(fk._foreignKeyReferenceDefinitionList.joinToString { QueryBuilder.quote(it.foreignColumnName) })
                    add(") ON UPDATE ${fk.onUpdate.name.replace("_", " ")} ON DELETE ${fk.onDelete.name.replace("_", " ")}")
                }.build())
            }

            if (foreignSize > 0) {
                for (i in 0..foreignSize - 1) {
                    codeBuilder.add("+ ${foreignKeyBlocks[i].S} + ${tableNameBlocks[i]} + ${referenceKeyBlocks[i].S}")
                }
                codeBuilder.add(" + ${");".S};\n")
            } else {
                codeBuilder.add(";\n")
            }

            addCode(codeBuilder.build())
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
            typeBuilder.`private final field`(it, "typeConverter${it.simpleName()}") { `=`("new \$T()", it) }
        }

        val globalTypeConverters = baseTableDefinition.globalTypeConverters.keys
        globalTypeConverters.forEach {
            typeBuilder.`private final field`(it, "global_typeConverter${it.simpleName()}")
        }
    }

    override fun addCode(code: CodeBlock.Builder): CodeBlock.Builder {
        // Constructor code
        val globalTypeConverters = baseTableDefinition.globalTypeConverters.keys
        globalTypeConverters.forEach {
            val def = baseTableDefinition.globalTypeConverters[it]
            val firstDef = def?.get(0)
            firstDef?.typeConverterElementNames?.forEach { elementName ->
                code.statement("global_typeConverter${it.simpleName()} " +
                    "= (\$T) holder.getTypeConverterForClass(\$T.class)", it, elementName)
            }
        }
        return code
    }
}

/**
 * Description:
 */
class ExistenceMethod(private val tableDefinition: BaseTableDefinition) : MethodDefinition {

    override val methodSpec
        get() = `override fun`(TypeName.BOOLEAN, "exists",
            param(tableDefinition.parameterClassName!!, ModelUtils.variable),
            param(ClassNames.DATABASE_WRAPPER, "wrapper")) {
            modifiers(public, final)
            code {
                // only quick check if enabled.
                var primaryColumn = tableDefinition.autoIncrementColumn
                if (primaryColumn == null) {
                    primaryColumn = tableDefinition.primaryColumnDefinitions[0]
                }
                primaryColumn.appendExistenceMethod(this)
                this
            }
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
            return `override fun`(String::class,
                if (isInsert) "getInsertStatementQuery" else "getCompiledStatementQuery") {
                modifiers(public, final)
                val isSingleAutoincrement = tableDefinition.hasAutoIncrement
                    && tableDefinition.columnDefinitions.size == 1 && isInsert
                `return`(codeBlock {
                    add("INSERT ")
                    if (!tableDefinition.insertConflictActionName.isEmpty()) {
                        add("OR ${tableDefinition.insertConflictActionName} ")
                    }
                    add("INTO ${QueryBuilder.quote(tableDefinition.tableName)}(")

                    tableDefinition.columnDefinitions.filter {
                        !it.isPrimaryKeyAutoIncrement && !it.isRowId || !isInsert || isSingleAutoincrement
                    }.forEachIndexed { index, columnDefinition ->
                        if (index > 0) add(",")
                        add(columnDefinition.insertStatementColumnName)

                    }
                    add(") VALUES (")

                    tableDefinition.columnDefinitions.filter { !it.isPrimaryKeyAutoIncrement && !it.isRowId || !isInsert }
                        .forEachIndexed { index, columnDefinition ->
                            if (index > 0) add(",")
                            add(columnDefinition.insertStatementValuesString)
                        }

                    if (isSingleAutoincrement) add("NULL")

                    add(")")
                }.S)
            }
        }
}

/**
 * Description:
 */
class LoadFromCursorMethod(private val baseTableDefinition: BaseTableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() = `override fun`(TypeName.VOID, "loadFromCursor",
            param(ClassNames.FLOW_CURSOR, PARAM_CURSOR),
            param(baseTableDefinition.parameterClassName!!, ModelUtils.variable)) {
            modifiers(public, final)
            val index = AtomicInteger(0)
            val nameAllocator = NameAllocator() // unique names
            baseTableDefinition.columnDefinitions.forEach {
                addCode(it.getLoadFromCursorMethod(true, index, nameAllocator))
                index.incrementAndGet()
            }

            if (baseTableDefinition is TableDefinition) {
                val foundDef = baseTableDefinition.oneToManyDefinitions.find { it.hasWrapper }
                foundDef?.let { foundDef.writeWrapperStatement(this) }

                code {
                    baseTableDefinition.oneToManyDefinitions
                        .filter { it.isLoad }
                        .forEach { it.writeLoad(this) }
                    this
                }
            }

            if (baseTableDefinition is TableDefinition && baseTableDefinition.implementsLoadFromCursorListener) {
                statement("${ModelUtils.variable}.onLoadFromCursor($PARAM_CURSOR)")
            }
            this
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
            val shouldWrite = tableDefinition.oneToManyDefinitions.any { it.isDelete }
            if (shouldWrite || tableDefinition.cachingEnabled) {
                return `override fun`(TypeName.BOOLEAN, "delete",
                    param(tableDefinition.elementClassName!!, ModelUtils.variable)) {
                    modifiers(public, final)
                    if (useWrapper) {
                        addParameter(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)
                    }
                    if (tableDefinition.cachingEnabled) {
                        statement("getModelCache().removeModel(getCachingId(${ModelUtils.variable}))")
                    }

                    statement("boolean successful = super.delete(${ModelUtils.variable}${wrapperCommaIfBaseModel(useWrapper)})")

                    if (!useWrapper) {
                        val foundDef = tableDefinition.oneToManyDefinitions.find { it.hasWrapper }
                        foundDef?.let { foundDef.writeWrapperStatement(this) }
                    }

                    tableDefinition.oneToManyDefinitions.forEach { it.writeDelete(this, useWrapper) }

                    `return`("successful")
                }
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
                var retType = TypeName.BOOLEAN
                var retStatement = "successful"
                when (methodName) {
                    METHOD_INSERT -> {
                        retType = ClassName.LONG
                        retStatement = "rowId"
                    }
                }

                return `override fun`(retType, methodName,
                    param(tableDefinition.elementClassName!!, ModelUtils.variable)) {
                    modifiers(public, final)

                    if (useWrapper) {
                        addParameter(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)
                    }
                    code {
                        if (methodName == METHOD_INSERT) {
                            add("long rowId = ")
                        } else if (methodName == METHOD_UPDATE || methodName == METHOD_SAVE) {
                            add("boolean successful = ")
                        }
                        statement("super.$methodName(${ModelUtils.variable}${wrapperCommaIfBaseModel(useWrapper)})")

                        if (tableDefinition.cachingEnabled) {
                            statement("getModelCache().addModel(getCachingId(${ModelUtils.variable}), ${ModelUtils.variable})")
                        }
                        this
                    }

                    // need wrapper access if we have wrapper param here.
                    if (!useWrapper) {
                        val foundDef = tableDefinition.oneToManyDefinitions.find { it.hasWrapper }
                        foundDef?.let { foundDef.writeWrapperStatement(this) }
                    }

                    for (oneToManyDefinition in tableDefinition.oneToManyDefinitions) {
                        when (methodName) {
                            METHOD_SAVE -> oneToManyDefinition.writeSave(this, useWrapper)
                            METHOD_UPDATE -> oneToManyDefinition.writeUpdate(this, useWrapper)
                            METHOD_INSERT -> oneToManyDefinition.writeInsert(this, useWrapper)
                        }
                    }

                    `return`(retStatement)
                }
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
        get() = `override fun`(ClassNames.OPERATOR_GROUP, "getPrimaryConditionClause",
            param(tableDefinition.parameterClassName!!, ModelUtils.variable)) {
            modifiers(public, final)
            code {
                statement("\$T clause = \$T.clause()", ClassNames.OPERATOR_GROUP, ClassNames.OPERATOR_GROUP)
                tableDefinition.primaryColumnDefinitions.forEach {
                    val codeBuilder = CodeBlock.builder()
                    it.appendPropertyComparisonAccessStatement(codeBuilder)
                    add(codeBuilder.build())
                }
                this
            }
            `return`("clause")
        }
}
