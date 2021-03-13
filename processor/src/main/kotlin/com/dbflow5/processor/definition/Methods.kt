package com.dbflow5.processor.definition

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.wrapperCommaIfBaseModel
import com.dbflow5.processor.utils.ModelUtils
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.processor.utils.codeBlock
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.quote
import com.grosner.kpoet.S
import com.grosner.kpoet.`=`
import com.grosner.kpoet.`for`
import com.grosner.kpoet.`private final field`
import com.grosner.kpoet.`return`
import com.grosner.kpoet.code
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
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
class BindToContentValuesMethod(private val entityDefinition: EntityDefinition,
                                private val isInsert: Boolean,
                                private val implementsContentValuesListener: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            val methodBuilder = MethodSpec.methodBuilder(if (isInsert) "bindToInsertValues" else "bindToContentValues")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ClassNames.CONTENT_VALUES, PARAM_CONTENT_VALUES)
                .addParameter(entityDefinition.parameterClassName, ModelUtils.variable)
                .returns(TypeName.VOID)

            var retMethodBuilder: MethodSpec.Builder? = methodBuilder

            if (isInsert) {
                entityDefinition.columnDefinitions.forEach {
                    if (it.type !is ColumnDefinition.Type.PrimaryAutoIncrement
                        && it.type !is ColumnDefinition.Type.RowId) {
                        methodBuilder.addCode(it.contentValuesStatement)
                    }
                }

                if (implementsContentValuesListener) {
                    methodBuilder.addStatement("\$L.onBindTo\$LValues(\$L)",
                        ModelUtils.variable, if (isInsert) "Insert" else "Content", PARAM_CONTENT_VALUES)
                }
            } else {
                if (entityDefinition.primaryKeyColumnBehavior.hasAutoIncrement
                    || entityDefinition.primaryKeyColumnBehavior.hasRowID) {
                    val autoIncrement = entityDefinition.primaryKeyColumnBehavior.associatedColumn
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
class BindToStatementMethod(private val tableDefinition: TableDefinition,
                            private val mode: Mode) : MethodDefinition {

    enum class Mode {
        INSERT {
            override val methodName = "bindToInsertStatement"

            override val sqlListenerName = "onBindToInsertStatement"
        },
        UPDATE {
            override val methodName = "bindToUpdateStatement"

            override val sqlListenerName = "onBindToUpdateStatement"
        },
        DELETE {
            override val methodName = "bindToDeleteStatement"

            override val sqlListenerName = "onBindToDeleteStatement"
        };

        abstract val methodName: String

        abstract val sqlListenerName: String
    }

    override val methodSpec: MethodSpec?
        get() {
            val methodBuilder = MethodSpec.methodBuilder(mode.methodName)
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ClassNames.DATABASE_STATEMENT, PARAM_STATEMENT)
                .addParameter(tableDefinition.parameterClassName,
                    ModelUtils.variable).returns(TypeName.VOID)

            // attach non rowid first, then go onto the WHERE clause
            when (mode) {
                Mode.INSERT -> {
                    val start = AtomicInteger(1)
                    tableDefinition.sqlColumnDefinitions
                        .forEach {
                            methodBuilder.addCode(it.getSQLiteStatementMethod(start))
                            start.incrementAndGet()
                        }
                }
                Mode.UPDATE -> {
                    val realCount = AtomicInteger(1)
                    // attach non rowid first, then go onto the WHERE clause
                    tableDefinition.sqlColumnDefinitions
                        .forEach {
                            methodBuilder.addCode(it.getSQLiteStatementMethod(realCount))
                            realCount.incrementAndGet()
                        }
                    tableDefinition.primaryColumnDefinitions.forEach {
                        methodBuilder.addCode(it.getSQLiteStatementMethod(realCount,
                            defineProperty = false))
                        realCount.incrementAndGet()
                    }
                }
                Mode.DELETE -> {
                    val realCount = AtomicInteger(1)
                    tableDefinition.primaryColumnDefinitions.forEach {
                        methodBuilder.addCode(it.getSQLiteStatementMethod(realCount))
                        realCount.incrementAndGet()
                    }
                }
            }

            if (tableDefinition.implementsSqlStatementListener) {
                methodBuilder.addStatement("${ModelUtils.variable}.${mode.sqlListenerName}($PARAM_STATEMENT)")
            }

            return methodBuilder.build()
        }

    companion object {

        val PARAM_STATEMENT = "statement"
    }
}

/**
 * Description:
 */
class CreationQueryMethod(private val tableDefinition: TableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() = `override fun`(String::class, "getCreationQuery") {
            modifiers(public, final)
            if (tableDefinition.type.isVirtual) {
                addCode("return ${
                    codeBlock {
                        add("CREATE VIRTUAL TABLE IF NOT EXISTS ${tableDefinition.associationalBehavior.name.quote()} USING ")
                        when (tableDefinition.type) {
                            TableDefinition.Type.FTS4 -> add("FTS4")
                            TableDefinition.Type.FTS3 -> add("FTS3")
                            else -> {
                                ProcessorManager.manager.logError("Invalid table type found ${tableDefinition.type}")
                            }
                        }
                        add("(")
                        // FTS4 uses column names directly.
                        add(tableDefinition.columnDefinitions.joinToString { it.columnName.quote() })
                        tableDefinition.ftsBehavior?.addContentTableCode(tableDefinition.columnDefinitions.isNotEmpty(), this)
                        add(")")
                    }.S
                };\n")
            } else {

                val foreignSize = tableDefinition.foreignKeyDefinitions.size

                val creationBuilder = codeBlock {
                    add("CREATE ${if (tableDefinition.temporary) "TEMP " else ""}TABLE IF NOT EXISTS ${tableDefinition.associationalBehavior.name.quote()}(")
                    add(tableDefinition.columnDefinitions.joinToString { it.creationName.toString() })
                    tableDefinition.uniqueGroupsDefinitions.forEach {
                        if (it.columnDefinitionList.isNotEmpty()) add(it.creationName)
                    }

                    if (!tableDefinition.primaryKeyColumnBehavior.hasAutoIncrement) {
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
                    .add("return \"$creationBuilder")

                tableDefinition.foreignKeyDefinitions.forEach { fk ->
                    val referencedTableDefinition = ProcessorManager.manager.getReferenceDefinition(tableDefinition.associationalBehavior.databaseTypeName, fk.referencedClassName)
                    if (referencedTableDefinition == null) {
                        fk.throwCannotFindReference()
                    } else {
                        codeBuilder.add(buildString {
                            append(", FOREIGN KEY(")
                            append(fk.referenceDefinitionList.joinToString { it.columnName.quote() })
                            append(") REFERENCES ")
                            append("${referencedTableDefinition.associationalBehavior.name} ")
                            append("(")
                            append(fk.referenceDefinitionList.joinToString { it.foreignColumnName.quote() })
                            append(") ON UPDATE ${fk.foreignKeyColumnBehavior!!.onUpdate.name.replace("_", " ")}")
                            append(" ON DELETE ${fk.foreignKeyColumnBehavior.onDelete.name.replace("_", " ")}")
                            if (fk.foreignKeyColumnBehavior.deferred) {
                                append(" DEFERRABLE INITIALLY DEFERRED")
                            }
                        })
                    }
                }
                if (foreignSize > 0) {
                    codeBuilder.add(")")
                }
                codeBuilder.add("\";\n")

                addCode(codeBuilder.build())
            }
        }
}

/**
 * Description: Writes out the custom type converter fields.
 */
class CustomTypeConverterPropertyMethod(private val entityDefinition: EntityDefinition)
    : TypeAdder, CodeAdder {

    override fun addToType(typeBuilder: TypeSpec.Builder) {
        val customTypeConverters = entityDefinition.associatedTypeConverters.keys
        customTypeConverters.forEach {
            typeBuilder.`private final field`(it, "typeConverter${it.simpleName()}") { `=`("new \$T()", it) }
        }

        val globalTypeConverters = entityDefinition.globalTypeConverters.keys
        globalTypeConverters.forEach {
            typeBuilder.`private final field`(it, "global_typeConverter${it.simpleName()}")
        }
    }

    override fun addCode(code: CodeBlock.Builder): CodeBlock.Builder {
        // Constructor code
        val globalTypeConverters = entityDefinition.globalTypeConverters.keys
        globalTypeConverters.forEach {
            val def = entityDefinition.globalTypeConverters[it]
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
class ExistenceMethod(private val tableDefinition: EntityDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            if (tableDefinition.primaryColumnDefinitions.isNotEmpty()) {
                val primaryColumn = tableDefinition.primaryKeyColumnBehavior.associatedColumn
                    ?: tableDefinition.primaryColumnDefinitions[0]
                if (primaryColumn.shouldWriteExistence()) {
                    return `override fun`(TypeName.BOOLEAN, "exists",
                        param(tableDefinition.parameterClassName!!, ModelUtils.variable),
                        param(ClassNames.DATABASE_WRAPPER, "wrapper")) {
                        modifiers(public, final)
                        code {
                            primaryColumn.appendExistenceMethod(this)
                            this
                        }
                    }
                }
            }
            return null
        }
}

/**
 * Description:
 */
class InsertStatementQueryMethod(private val tableDefinition: TableDefinition,
                                 private val mode: Mode) : MethodDefinition {

    enum class Mode {
        INSERT,
        SAVE
    }

    override val methodSpec: MethodSpec?
        get() {
            return `override fun`(String::class,
                when (mode) {
                    Mode.INSERT -> "getInsertStatementQuery"
                    Mode.SAVE -> "getSaveStatementQuery"
                }) {
                modifiers(public, final)
                `return`(codeBlock {
                    add("INSERT ")
                    if (mode != Mode.SAVE) {
                        if (!tableDefinition.insertConflictActionName.isEmpty()) {
                            add("OR ${tableDefinition.insertConflictActionName} ")
                        }
                    } else {
                        add("OR ${ConflictAction.REPLACE} ")
                    }
                    add("INTO ${tableDefinition.associationalBehavior.name.quote()}(")

                    tableDefinition.sqlColumnDefinitions
                        .forEachIndexed { index, columnDefinition ->
                            if (index > 0) add(",")
                            add(columnDefinition.insertStatementColumnName)

                        }

                    add(") VALUES (")

                    tableDefinition.sqlColumnDefinitions
                        .forEachIndexed { index, columnDefinition ->
                            if (index > 0) add(",")
                            add(columnDefinition.insertStatementValuesString)
                        }

                    add(")")
                }.S)
            }
        }
}

class UpdateStatementQueryMethod(private val tableDefinition: TableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            return `override fun`(String::class, "getUpdateStatementQuery") {
                modifiers(public, final)
                `return`(codeBlock {
                    add("UPDATE")
                    if (!tableDefinition.updateConflictActionName.isEmpty()) {
                        add(" OR ${tableDefinition.updateConflictActionName}")
                    }
                    add(" ${tableDefinition.associationalBehavior.name.quote()} SET ")

                    // can only change non primary key values.
                    tableDefinition.sqlColumnDefinitions
                        .forEachIndexed { index, columnDefinition ->
                            if (index > 0) add(",")
                            add(columnDefinition.updateStatementBlock)

                        }
                    add(" WHERE ")

                    // primary key values used as WHERE
                    tableDefinition.columnDefinitions
                        .filter { it.type.isPrimaryField || tableDefinition.type.isVirtual }
                        .forEachIndexed { index, columnDefinition ->
                            if (index > 0) add(" AND ")
                            add(columnDefinition.updateStatementBlock)
                        }
                    this
                }.S)
            }
        }
}

class DeleteStatementQueryMethod(private val tableDefinition: TableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec?
        get() {
            return `override fun`(String::class, "getDeleteStatementQuery") {
                modifiers(public, final)
                `return`(codeBlock {
                    add("DELETE FROM ${tableDefinition.associationalBehavior.name.quote()} WHERE ")

                    // primary key values used as WHERE
                    tableDefinition.columnDefinitions
                        .filter { it.type.isPrimaryField || tableDefinition.type.isVirtual }
                        .forEachIndexed { index, columnDefinition ->
                            if (index > 0) add(" AND ")
                            add(columnDefinition.updateStatementBlock)
                        }
                    this
                }.S)
            }
        }
}

/**
 * Description:
 */
class LoadFromCursorMethod(private val entityDefinition: EntityDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() = `override fun`(entityDefinition.parameterClassName!!, "loadFromCursor",
            param(ClassNames.FLOW_CURSOR, PARAM_CURSOR),
            param(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)) {
            modifiers(public, final)
            statement("\$1T ${ModelUtils.variable} = new \$1T()", entityDefinition.parameterClassName)
            val index = AtomicInteger(0)
            val nameAllocator = NameAllocator() // unique names
            entityDefinition.columnDefinitions.forEach {
                addCode(it.getLoadFromCursorMethod(true, index, nameAllocator))
                index.incrementAndGet()
            }

            if (entityDefinition is TableDefinition) {
                code {
                    entityDefinition.oneToManyDefinitions
                        .filter { it.isLoad }
                        .forEach { it.writeLoad(this) }
                    this
                }
            }

            if (entityDefinition.implementsLoadFromCursorListener) {
                statement("${ModelUtils.variable}.onLoadFromCursor($PARAM_CURSOR)")
            }
            `return`(ModelUtils.variable)
        }


    companion object {

        val PARAM_CURSOR = "cursor"
    }
}

/**
 * Description:
 */
class OneToManyDeleteMethod(private val tableDefinition: TableDefinition,
                            private val isPlural: Boolean = false) : MethodDefinition {

    private val variableName = if (isPlural) "models" else ModelUtils.variable
    private val typeName: TypeName = if (!isPlural) tableDefinition.elementClassName!! else
        ParameterizedTypeName.get(ClassName.get(Collection::class.java), WildcardTypeName.subtypeOf(tableDefinition.elementClassName!!))

    private val methodName = "delete${if (isPlural) "All" else ""}"

    override val methodSpec: MethodSpec?
        get() {
            val shouldWrite = tableDefinition.oneToManyDefinitions.any { it.isDelete }
            if (shouldWrite || tableDefinition.cachingBehavior.cachingEnabled) {
                val returnTypeName = if (isPlural) TypeName.LONG else TypeName.BOOLEAN
                return `override fun`(returnTypeName, methodName,
                    param(typeName, variableName)) {
                    modifiers(public, final)
                    addParameter(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)
                    if (tableDefinition.cachingBehavior.cachingEnabled) {
                        statement("cacheAdapter.removeModel${if (isPlural) "s" else ""}FromCache(${variableName})")
                    }

                    statement("\$T successful = super.${methodName}(${variableName}${wrapperCommaIfBaseModel(true)})", returnTypeName)

                    if (isPlural && tableDefinition.oneToManyDefinitions.isNotEmpty()) {
                        `for`("\$T model: models", tableDefinition.elementClassName!!) {
                            tableDefinition.oneToManyDefinitions.forEach { it.writeDelete(this) }
                            this
                        }
                    } else {
                        tableDefinition.oneToManyDefinitions.forEach { it.writeDelete(this) }
                    }

                    `return`("successful")
                }
            }
            return null
        }
}

/**
 * Description: Overrides the save, update, and insert methods if the [com.dbflow5.annotation.OneToMany.Method.SAVE] is used.
 */
class OneToManySaveMethod(private val tableDefinition: TableDefinition,
                          private val methodName: String,
                          private val isPlural: Boolean = false) : MethodDefinition {

    private val variableName = if (isPlural) "models" else ModelUtils.variable
    private val typeName: TypeName = if (!isPlural) tableDefinition.elementClassName!! else
        ParameterizedTypeName.get(ClassName.get(Collection::class.java), WildcardTypeName.subtypeOf(tableDefinition.elementClassName!!))

    private val fullMethodName = "$methodName${if (isPlural) "All" else ""}"

    override val methodSpec: MethodSpec?
        get() {
            if (!tableDefinition.oneToManyDefinitions.isEmpty() || tableDefinition.cachingBehavior.cachingEnabled) {
                var retType = TypeName.BOOLEAN
                var retStatement = "successful"
                if (isPlural) {
                    retType = ClassName.LONG
                    retStatement = "count"
                } else if (fullMethodName == METHOD_INSERT) {
                    retType = ClassName.LONG
                    retStatement = "rowId"
                }

                return `override fun`(
                    retType, fullMethodName,
                    param(typeName, variableName),
                ) {
                    modifiers(public, final)
                    addParameter(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)
                    code {
                        if (isPlural) {
                            add("long count = ")
                        } else if (fullMethodName == METHOD_INSERT) {
                            add("long rowId = ")
                        } else if (fullMethodName == METHOD_UPDATE || fullMethodName == METHOD_SAVE) {
                            add("boolean successful = ")
                        }
                        statement("super.$fullMethodName(${variableName}${wrapperCommaIfBaseModel(true)})")

                        if (tableDefinition.cachingBehavior.cachingEnabled) {
                            statement("cacheAdapter.storeModel${if (isPlural) "s" else ""}InCache(${variableName})")
                        }
                        this
                    }

                    val filteredDefinitions = tableDefinition.oneToManyDefinitions.filter { it.isSave }

                    fun saveDefinitions() {
                        filteredDefinitions.forEach { oneToManyDefinition ->
                            when (methodName) {
                                METHOD_SAVE -> oneToManyDefinition.writeSave(this)
                                METHOD_UPDATE -> oneToManyDefinition.writeUpdate(this)
                                METHOD_INSERT -> oneToManyDefinition.writeInsert(this)
                            }
                        }
                    }
                    if (isPlural && filteredDefinitions.isNotEmpty()) {
                        `for`("\$T model: models", tableDefinition.elementClassName!!) {
                            saveDefinitions()
                            this
                        }
                    } else {
                        saveDefinitions()
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
class PrimaryConditionMethod(private val tableDefinition: EntityDefinition) : MethodDefinition {

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
