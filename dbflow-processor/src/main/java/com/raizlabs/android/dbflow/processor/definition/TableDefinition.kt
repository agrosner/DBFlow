package com.raizlabs.android.dbflow.processor.definition

import com.google.common.collect.Lists
import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ColumnValidator
import com.raizlabs.android.dbflow.processor.OneToManyValidator
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.DefinitionUtils
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition
import com.raizlabs.android.dbflow.processor.utils.*
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * Description: Used in writing ModelAdapters
 */
class TableDefinition(manager: ProcessorManager, element: TypeElement) : BaseTableDefinition(element, manager) {

    var tableName: String? = null

    var databaseTypeName: TypeName? = null

    var insertConflictActionName: String = ""

    var updateConflictActionName: String = ""

    var primaryKeyConflictActionName: String = ""

    val _primaryColumnDefinitions = mutableListOf<ColumnDefinition>()
    val foreignKeyDefinitions = mutableListOf<ForeignKeyColumnDefinition>()
    val uniqueGroupsDefinitions = mutableListOf<UniqueGroupsDefinition>()
    val indexGroupsDefinitions = mutableListOf<IndexGroupsDefinition>()

    var implementsContentValuesListener = false

    var implementsSqlStatementListener = false

    var implementsLoadFromCursorListener = false

    private val methods: Array<MethodDefinition>

    var cachingEnabled = false
    var cacheSize: Int = 0
    var customCacheFieldName: String? = null
    var customMultiCacheFieldName: String? = null

    var allFields = false
    var useIsForPrivateBooleans: Boolean = false

    val columnMap = mutableMapOf<String, ColumnDefinition>()

    var columnUniqueMap = mutableMapOf<Int, MutableSet<ColumnDefinition>>()

    var oneToManyDefinitions = mutableListOf<OneToManyDefinition>()

    var inheritedColumnMap = hashMapOf<String, InheritedColumn>()
    var inheritedFieldNameList = mutableListOf<String>()
    var inheritedPrimaryKeyMap = hashMapOf<String, InheritedPrimaryKey>()

    var hasPrimaryConstructor = false

    init {

        element.annotation<Table>()?.let { table ->
            this.tableName = table.name

            if (tableName == null || tableName!!.isEmpty()) {
                tableName = element.simpleName.toString()
            }

            try {
                table.database
            } catch (mte: MirroredTypeException) {
                databaseTypeName = TypeName.get(mte.typeMirror)
            }

            cachingEnabled = table.cachingEnabled
            cacheSize = table.cacheSize

            orderedCursorLookUp = table.orderedCursorLookUp
            assignDefaultValuesFromCursor = table.assignDefaultValuesFromCursor

            allFields = table.allFields
            useIsForPrivateBooleans = table.useBooleanGetterSetters

            elementClassName?.let { databaseTypeName?.let { it1 -> manager.addModelToDatabase(it, it1) } }


            val inheritedColumns = table.inheritedColumns
            inheritedColumns.forEach {
                if (inheritedFieldNameList.contains(it.fieldName)) {
                    manager.logError("A duplicate inherited column with name %1s was found for %1s",
                        it.fieldName, tableName)
                }
                inheritedFieldNameList.add(it.fieldName)
                inheritedColumnMap.put(it.fieldName, it)
            }

            val inheritedPrimaryKeys = table.inheritedPrimaryKeys
            inheritedPrimaryKeys.forEach {
                if (inheritedFieldNameList.contains(it.fieldName)) {
                    manager.logError("A duplicate inherited column with name %1s was found for %1s",
                        it.fieldName, tableName)
                }
                inheritedFieldNameList.add(it.fieldName)
                inheritedPrimaryKeyMap.put(it.fieldName, it)
            }

            implementsLoadFromCursorListener = element.implementsClass(manager.processingEnvironment,
                ClassNames.LOAD_FROM_CURSOR_LISTENER)

            implementsContentValuesListener = element.implementsClass(manager.processingEnvironment,
                ClassNames.CONTENT_VALUES_LISTENER)

            implementsSqlStatementListener = element.implementsClass(manager.processingEnvironment,
                ClassNames.SQLITE_STATEMENT_LISTENER)
        }

        methods = arrayOf(BindToContentValuesMethod(this, true, implementsContentValuesListener),
            BindToContentValuesMethod(this, false, implementsContentValuesListener),
            BindToStatementMethod(this, true), BindToStatementMethod(this, false),
            InsertStatementQueryMethod(this, true), InsertStatementQueryMethod(this, false),
            CreationQueryMethod(this), LoadFromCursorMethod(this), ExistenceMethod(this),
            PrimaryConditionMethod(this), OneToManyDeleteMethod(this, false),
            OneToManyDeleteMethod(this, true),
            OneToManySaveMethod(this, OneToManySaveMethod.METHOD_SAVE, false),
            OneToManySaveMethod(this, OneToManySaveMethod.METHOD_INSERT, false),
            OneToManySaveMethod(this, OneToManySaveMethod.METHOD_UPDATE, false),
            OneToManySaveMethod(this, OneToManySaveMethod.METHOD_SAVE, true),
            OneToManySaveMethod(this, OneToManySaveMethod.METHOD_INSERT, true),
            OneToManySaveMethod(this, OneToManySaveMethod.METHOD_UPDATE, true))
    }

    override fun prepareForWrite() {
        columnDefinitions = ArrayList<ColumnDefinition>()
        columnMap.clear()
        classElementLookUpMap.clear()
        _primaryColumnDefinitions.clear()
        uniqueGroupsDefinitions.clear()
        indexGroupsDefinitions.clear()
        foreignKeyDefinitions.clear()
        columnUniqueMap.clear()
        oneToManyDefinitions.clear()
        customCacheFieldName = null
        customMultiCacheFieldName = null

        val table = element.getAnnotation(Table::class.java)
        if (table != null) {
            databaseDefinition = manager.getDatabaseHolderDefinition(databaseTypeName)?.databaseDefinition
            if (databaseDefinition == null) {
                manager.logError("DatabaseDefinition was null for : $tableName for db type: $databaseTypeName")
            }
            databaseDefinition?.let {

                setOutputClassName("${it.classSeparator}Table")

                // globular default
                var insertConflict = table.insertConflict
                if (insertConflict == ConflictAction.NONE && it.insertConflict != ConflictAction.NONE) {
                    insertConflict = it.insertConflict ?: ConflictAction.NONE
                }

                var updateConflict = table.updateConflict
                if (updateConflict == ConflictAction.NONE && it.updateConflict != ConflictAction.NONE) {
                    updateConflict = it.updateConflict ?: ConflictAction.NONE
                }

                val primaryKeyConflict = table.primaryKeyConflict

                insertConflictActionName = if (insertConflict == ConflictAction.NONE) "" else insertConflict.name
                updateConflictActionName = if (updateConflict == ConflictAction.NONE) "" else updateConflict.name
                primaryKeyConflictActionName = if (primaryKeyConflict == ConflictAction.NONE) "" else primaryKeyConflict.name
            }

            typeElement?.let { createColumnDefinitions(it) }

            val groups = table.uniqueColumnGroups
            var uniqueNumbersSet: MutableSet<Int> = HashSet()
            for (uniqueGroup in groups) {
                if (uniqueNumbersSet.contains(uniqueGroup.groupNumber)) {
                    manager.logError("A duplicate unique group with number %1s was found for %1s", uniqueGroup.groupNumber, tableName)
                }
                val definition = UniqueGroupsDefinition(uniqueGroup)
                columnDefinitions.filter { it.uniqueGroups.contains(definition.number) }
                    .forEach { definition.addColumnDefinition(it) }
                uniqueGroupsDefinitions.add(definition)
                uniqueNumbersSet.add(uniqueGroup.groupNumber)
            }

            val indexGroups = table.indexGroups
            uniqueNumbersSet = HashSet<Int>()
            for (indexGroup in indexGroups) {
                if (uniqueNumbersSet.contains(indexGroup.number)) {
                    manager.logError(TableDefinition::class, "A duplicate unique index number %1s was found for %1s", indexGroup.number, elementName)
                }
                val definition = IndexGroupsDefinition(this, indexGroup)
                columnDefinitions.filter { it.indexGroups.contains(definition.indexNumber) }
                    .forEach { definition.columnDefinitionList.add(it) }
                indexGroupsDefinitions.add(definition)
                uniqueNumbersSet.add(indexGroup.number)
            }
        }

    }

    override fun createColumnDefinitions(typeElement: TypeElement) {
        val elements = ElementUtility.getAllElements(typeElement, manager)

        for (element in elements) {
            classElementLookUpMap.put(element.simpleName.toString(), element)
            if (element is ExecutableElement && element.parameters.isEmpty()
                && element.simpleName.toString() == "<init>"
                && element.enclosingElement == typeElement
                && !element.modifiers.contains(Modifier.PRIVATE)) {
                hasPrimaryConstructor = true
            }
        }

        val columnValidator = ColumnValidator()
        val oneToManyValidator = OneToManyValidator()
        elements.forEach { element ->
            // no private static or final fields for all columns, or any inherited columns here.
            val isAllFields = ElementUtility.isValidAllFields(allFields, element)

            // package private, will generate helper
            val isPackagePrivate = ElementUtility.isPackagePrivate(element)
            val isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, element, this.element)

            val isForeign = element.annotation<ForeignKey>() != null
            val isPrimary = element.annotation<PrimaryKey>() != null
            val isInherited = inheritedColumnMap.containsKey(element.simpleName.toString())
            val isInheritedPrimaryKey = inheritedPrimaryKeyMap.containsKey(element.simpleName.toString())
            if (element.annotation<Column>() != null || isForeign || isPrimary
                || isAllFields || isInherited || isInheritedPrimaryKey) {

                val columnDefinition: ColumnDefinition
                if (isInheritedPrimaryKey) {
                    val inherited = inheritedPrimaryKeyMap[element.simpleName.toString()]
                    columnDefinition = ColumnDefinition(manager, element, this, isPackagePrivateNotInSamePackage,
                        inherited?.column, inherited?.primaryKey)
                } else if (isInherited) {
                    val inherited = inheritedColumnMap[element.simpleName.toString()]
                    columnDefinition = ColumnDefinition(manager, element, this, isPackagePrivateNotInSamePackage,
                        inherited?.column, null)
                } else if (isForeign) {
                    columnDefinition = ForeignKeyColumnDefinition(manager, this,
                        element, isPackagePrivateNotInSamePackage)
                } else {
                    columnDefinition = ColumnDefinition(manager, element,
                        this, isPackagePrivateNotInSamePackage)
                }

                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition)
                    columnMap.put(columnDefinition.columnName, columnDefinition)
                    if (columnDefinition.isPrimaryKey) {
                        _primaryColumnDefinitions.add(columnDefinition)
                    } else if (columnDefinition.isPrimaryKeyAutoIncrement) {
                        autoIncrementColumn = columnDefinition
                        hasAutoIncrement = true
                    } else if (columnDefinition.isRowId) {
                        autoIncrementColumn = columnDefinition
                        hasRowID = true
                    }

                    if (columnDefinition is ForeignKeyColumnDefinition) {
                        foreignKeyDefinitions.add(columnDefinition)
                    }

                    if (!columnDefinition.uniqueGroups.isEmpty()) {
                        val groups = columnDefinition.uniqueGroups
                        for (group in groups) {
                            var groupList = columnUniqueMap[group]
                            if (groupList == null) {
                                groupList = mutableSetOf()
                                columnUniqueMap.put(group, groupList)
                            }
                            groupList.add(columnDefinition)
                        }
                    }

                    if (isPackagePrivate) {
                        packagePrivateList.add(columnDefinition)
                    }
                }
            } else if (element.annotation<OneToMany>() != null) {
                val oneToManyDefinition = OneToManyDefinition(element as ExecutableElement, manager)
                if (oneToManyValidator.validate(manager, oneToManyDefinition)) {
                    oneToManyDefinitions.add(oneToManyDefinition)
                }
            } else if (element.annotation<ModelCacheField>() != null) {
                ensureVisibleStatic(element, typeElement, "ModelCacheField")
                if (!customCacheFieldName.isNullOrEmpty()) {
                    manager.logError("ModelCacheField can only be declared once from: " + typeElement)
                } else {
                    customCacheFieldName = element.simpleName.toString()
                }
            } else if (element.annotation<MultiCacheField>() != null) {
                ensureVisibleStatic(element, typeElement, "MultiCacheField")
                if (!customMultiCacheFieldName.isNullOrEmpty()) {
                    manager.logError("MultiCacheField can only be declared once from: " + typeElement)
                } else {
                    customMultiCacheFieldName = element.simpleName.toString()
                }
            }
        }
    }

    override val primaryColumnDefinitions: List<ColumnDefinition>
        get() = if (autoIncrementColumn != null) {
            Lists.newArrayList(autoIncrementColumn!!)
        } else {
            _primaryColumnDefinitions
        }

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        typeBuilder.apply {

            writeGetModelClass(this, elementClassName)
            writeConstructor(this)

            `override fun`(String::class, "getTableName") {
                modifiers(public, final)
                `return`(QueryBuilder.quote(tableName).S)
            }

            `override fun`(elementClassName!!, "newInstance") {
                modifiers(public, final)
                `return`("new \$T()", elementClassName)
            }

            if (updateConflictActionName.isNotEmpty()) {
                `override fun`(ClassNames.CONFLICT_ACTION, "getUpdateOnConflictAction") {
                    modifiers(public, final)
                    `return`("\$T.$updateConflictActionName", ClassNames.CONFLICT_ACTION)
                }
            }

            if (insertConflictActionName.isNotEmpty()) {
                `override fun`(ClassNames.CONFLICT_ACTION, "getInsertOnConflictAction") {
                    modifiers(public, final)
                    `return`("\$T.$insertConflictActionName", ClassNames.CONFLICT_ACTION)
                }
            }

            val paramColumnName = "columnName"
            val getPropertiesBuilder = CodeBlock.builder()

            `override fun`(ClassNames.PROPERTY, "getProperty",
                param(String::class, paramColumnName)) {
                modifiers(public, final)
                statement("$paramColumnName = \$T.quoteIfNeeded($paramColumnName)", ClassName.get(QueryBuilder::class.java))

                switch("($paramColumnName)") {
                    columnDefinitions.indices.forEach { i ->
                        if (i > 0) {
                            getPropertiesBuilder.add(",")
                        }
                        val columnDefinition = columnDefinitions[i]
                        elementClassName?.let { columnDefinition.addPropertyDefinition(typeBuilder, it) }
                        columnDefinition.addPropertyCase(this)
                        columnDefinition.addColumnName(getPropertiesBuilder)
                    }

                    default {
                        `throw new`(IllegalArgumentException::class, "Invalid column name passed. Ensure you are calling the correct table's column")
                    }
                }
            }

            `public static final field`(ArrayTypeName.of(ClassNames.IPROPERTY), "ALL_COLUMN_PROPERTIES") {
                `=`("new \$T[]{\$L}", ClassNames.IPROPERTY, getPropertiesBuilder.build().toString())
            }

            // add index properties here
            for (indexGroupsDefinition in indexGroupsDefinitions) {
                addField(indexGroupsDefinition.fieldSpec)
            }

            if (hasAutoIncrement || hasRowID) {
                val autoIncrement = autoIncrementColumn
                autoIncrement?.let {
                    `override fun`(TypeName.VOID, "updateAutoIncrement", param(elementClassName!!, ModelUtils.variable),
                        param(Number::class, "id")) {
                        modifiers(public, final)
                        addCode(autoIncrement.updateAutoIncrementMethod)
                    }

                    `override fun`(Number::class, "getAutoIncrementingId", param(elementClassName!!, ModelUtils.variable)) {
                        modifiers(public, final)
                        addCode(autoIncrement.getSimpleAccessString())
                    }
                    `override fun`(String::class, "getAutoIncrementingColumnName") {
                        modifiers(public, final)
                        `return`(QueryBuilder.stripQuotes(autoIncrement.columnName).S)
                    }
                }
            }

            val saveForeignKeyFields = columnDefinitions
                .filter { (it is ForeignKeyColumnDefinition) && it.saveForeignKeyModel }
                .map { it as ForeignKeyColumnDefinition }
            if (saveForeignKeyFields.isNotEmpty()) {
                val code = CodeBlock.builder()
                saveForeignKeyFields.forEach { it.appendSaveMethod(code) }

                `override fun`(TypeName.VOID, "saveForeignKeys", param(elementClassName!!, ModelUtils.variable),
                    param(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)) {
                    modifiers(public, final)
                    addCode(code.build())
                }
            }

            val deleteForeignKeyFields = columnDefinitions
                .filter { (it is ForeignKeyColumnDefinition) && it.deleteForeignKeyModel }
                .map { it as ForeignKeyColumnDefinition }
            if (deleteForeignKeyFields.isNotEmpty()) {
                val code = CodeBlock.builder()
                deleteForeignKeyFields.forEach { it.appendDeleteMethod(code) }

                `override fun`(TypeName.VOID, "deleteForeignKeys", param(elementClassName!!, ModelUtils.variable),
                    param(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)) {
                    modifiers(public, final)
                    addCode(code.build())
                }
            }

            `override fun`(ArrayTypeName.of(ClassNames.IPROPERTY), "getAllColumnProperties") {
                modifiers(public, final)
                `return`("ALL_COLUMN_PROPERTIES")
            }

            if (cachingEnabled) {

                val singlePrimaryKey = primaryColumnDefinitions.size == 1

                `override fun`(ClassNames.SINGLE_MODEL_LOADER, "createSingleModelLoader") {
                    modifiers(public, final)
                    addStatement("return new \$T<>(getModelClass())",
                        if (singlePrimaryKey)
                            ClassNames.SINGLE_KEY_CACHEABLE_MODEL_LOADER
                        else
                            ClassNames.CACHEABLE_MODEL_LOADER)
                }
                `override fun`(ClassNames.LIST_MODEL_LOADER, "createListModelLoader") {
                    modifiers(public, final)
                    `return`("new \$T<>(getModelClass())",
                        if (singlePrimaryKey)
                            ClassNames.SINGLE_KEY_CACHEABLE_LIST_MODEL_LOADER
                        else
                            ClassNames.CACHEABLE_LIST_MODEL_LOADER)
                }
                `override fun`(ParameterizedTypeName.get(ClassNames.CACHEABLE_LIST_MODEL_SAVER, elementClassName),
                    "createListModelSaver") {
                    modifiers(protected)
                    `return`("new \$T<>(getModelSaver())", ClassNames.CACHEABLE_LIST_MODEL_SAVER)
                }
                `override fun`(TypeName.BOOLEAN, "cachingEnabled") {
                    modifiers(public, final)
                    `return`(true.L)
                }

                val primaryColumns = primaryColumnDefinitions
                if (primaryColumns.size > 1) {
                    `override fun`(ArrayTypeName.of(Any::class.java), "getCachingColumnValuesFromModel",
                        param(ArrayTypeName.of(Any::class.java), "inValues"),
                        param(elementClassName!!, ModelUtils.variable)) {
                        modifiers(public, final)
                        for (i in primaryColumns.indices) {
                            val column = primaryColumns[i]
                            addCode(column.getColumnAccessString(i))
                        }

                        `return`("inValues")
                    }

                    `override fun`(ArrayTypeName.of(Any::class.java), "getCachingColumnValuesFromCursor",
                        param(ArrayTypeName.of(Any::class.java), "inValues"),
                        param(ClassNames.FLOW_CURSOR, "cursor")) {
                        modifiers(public, final)
                        for (i in primaryColumns.indices) {
                            val column = primaryColumns[i]
                            val method = DefinitionUtils.getLoadFromCursorMethodString(column.elementTypeName, column.wrapperTypeName)
                            statement("inValues[$i] = ${LoadFromCursorMethod.PARAM_CURSOR}" +
                                ".$method(${LoadFromCursorMethod.PARAM_CURSOR}.getColumnIndex(${column.columnName.S}))")
                        }
                        `return`("inValues")
                    }
                } else {
                    // single primary key
                    `override fun`(Any::class, "getCachingColumnValueFromModel",
                        param(elementClassName!!, ModelUtils.variable)) {
                        modifiers(public, final)
                        addCode(primaryColumns[0].getSimpleAccessString())
                    }

                    `override fun`(Any::class, "getCachingColumnValueFromCursor", param(ClassNames.FLOW_CURSOR, "cursor")) {
                        modifiers(public, final)
                        val column = primaryColumns[0]
                        val method = DefinitionUtils.getLoadFromCursorMethodString(column.elementTypeName, column.wrapperTypeName)
                        `return`("${LoadFromCursorMethod.PARAM_CURSOR}.$method(${LoadFromCursorMethod.PARAM_CURSOR}.getColumnIndex(${column.columnName.S}))")
                    }
                    `override fun`(Any::class, "getCachingId", param(elementClassName!!, ModelUtils.variable)) {
                        modifiers(public, final)
                        `return`("getCachingColumnValueFromModel(${ModelUtils.variable})")
                    }
                }

                `override fun`(ArrayTypeName.of(ClassName.get(String::class.java)), "createCachingColumns") {
                    modifiers(public, final)
                    `return`("new String[]{${primaryColumns.joinToString { QueryBuilder.quoteIfNeeded(it.columnName).S }}}")
                }

                if (cacheSize != Table.DEFAULT_CACHE_SIZE) {
                    `override fun`(TypeName.INT, "getCacheSize") {
                        modifiers(public, final)
                        `return`(cacheSize.L)
                    }
                }

                if (!customCacheFieldName.isNullOrEmpty()) {
                    `override fun`(ParameterizedTypeName.get(ClassNames.MODEL_CACHE, elementClassName,
                        WildcardTypeName.subtypeOf(Any::class.java)), "createModelCache") {
                        modifiers(public, final)
                        `return`("\$T.$customCacheFieldName", elementClassName)
                    }
                }

                if (!customMultiCacheFieldName.isNullOrEmpty()) {
                    `override fun`(ParameterizedTypeName.get(ClassNames.MULTI_KEY_CACHE_CONVERTER,
                        WildcardTypeName.subtypeOf(Any::class.java)), "getCacheConverter") {
                        modifiers(public, final)
                        `return`("\$T.$customMultiCacheFieldName", elementClassName)
                    }
                }

                if (foreignKeyDefinitions.isNotEmpty()) {
                    `override fun`(TypeName.VOID, "reloadRelationships",
                        param(elementClassName!!, ModelUtils.variable),
                        param(ClassNames.FLOW_CURSOR, LoadFromCursorMethod.PARAM_CURSOR)) {
                        modifiers(public, final)
                        code {
                            val noIndex = AtomicInteger(-1)
                            val nameAllocator = NameAllocator()
                            foreignKeyDefinitions.forEach { add(it.getLoadFromCursorMethod(false, noIndex, nameAllocator)) }
                            this
                        }
                    }
                }
            }
        }

        methods.mapNotNull { it.methodSpec }
            .forEach { typeBuilder.addMethod(it) }
    }
}
