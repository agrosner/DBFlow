package com.dbflow5.processor.definition

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.InheritedColumn
import com.dbflow5.annotation.InheritedPrimaryKey
import com.dbflow5.annotation.ModelCacheField
import com.dbflow5.annotation.MultiCacheField
import com.dbflow5.annotation.OneToMany
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.isNotNullOrEmpty
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ColumnValidator
import com.dbflow5.processor.OneToManyValidator
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.BindToStatementMethod.Mode.*
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.DefinitionUtils
import com.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.ModelUtils
import com.dbflow5.processor.utils.ModelUtils.wrapper
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.ensureVisibleStatic
import com.dbflow5.processor.utils.implementsClass
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.quote
import com.grosner.kpoet.L
import com.grosner.kpoet.S
import com.grosner.kpoet.`=`
import com.grosner.kpoet.`public static final field`
import com.grosner.kpoet.`return`
import com.grosner.kpoet.`throw new`
import com.grosner.kpoet.code
import com.grosner.kpoet.default
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.protected
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.grosner.kpoet.switch
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
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

    var insertConflictActionName: String = ""

    var updateConflictActionName: String = ""

    var primaryKeyConflictActionName: String = ""

    val _primaryColumnDefinitions = mutableListOf<ColumnDefinition>()
    val foreignKeyDefinitions = mutableListOf<ReferenceColumnDefinition>()
    val columnMapDefinitions = mutableListOf<ReferenceColumnDefinition>()
    val uniqueGroupsDefinitions = mutableListOf<UniqueGroupsDefinition>()
    val indexGroupsDefinitions = mutableListOf<IndexGroupsDefinition>()

    var implementsContentValuesListener = false

    var implementsSqlStatementListener = false

    var implementsLoadFromCursorListener = false

    private val methods: Array<MethodDefinition>
    private val contentValueMethods: Array<MethodDefinition>

    var cachingEnabled = false
    var customCacheFieldName: String? = null
    var customMultiCacheFieldName: String? = null
    var customCacheSize = 0

    var createWithDatabase = true

    var allFields = false
    var useIsForPrivateBooleans: Boolean = false
    var generateContentValues = false

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

            generateContentValues = table.generateContentValues
            cachingEnabled = table.cachingEnabled
            customCacheSize = table.cacheSize

            orderedCursorLookUp = table.orderedCursorLookUp
            assignDefaultValuesFromCursor = table.assignDefaultValuesFromCursor

            createWithDatabase = table.createWithDatabase

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
                    com.dbflow5.processor.ClassNames.LOAD_FROM_CURSOR_LISTENER)

            implementsContentValuesListener = element.implementsClass(manager.processingEnvironment,
                    com.dbflow5.processor.ClassNames.CONTENT_VALUES_LISTENER)

            implementsSqlStatementListener = element.implementsClass(manager.processingEnvironment,
                    com.dbflow5.processor.ClassNames.SQLITE_STATEMENT_LISTENER)
        }

        contentValueMethods = arrayOf(BindToContentValuesMethod(this, true, implementsContentValuesListener),
                BindToContentValuesMethod(this, false, implementsContentValuesListener))

        methods = arrayOf(
                BindToStatementMethod(this, INSERT),
                BindToStatementMethod(this, UPDATE),
                BindToStatementMethod(this, DELETE),
                InsertStatementQueryMethod(this, InsertStatementQueryMethod.Mode.INSERT),
                InsertStatementQueryMethod(this, InsertStatementQueryMethod.Mode.SAVE),
                UpdateStatementQueryMethod(this),
                DeleteStatementQueryMethod(this),
                CreationQueryMethod(this),
                LoadFromCursorMethod(this),
                ExistenceMethod(this),
                PrimaryConditionMethod(this),
                OneToManyDeleteMethod(this),
                OneToManySaveMethod(this, OneToManySaveMethod.METHOD_SAVE),
                OneToManySaveMethod(this, OneToManySaveMethod.METHOD_INSERT),
                OneToManySaveMethod(this, OneToManySaveMethod.METHOD_UPDATE)
        )
    }

    override fun prepareForWrite() {
        columnDefinitions = arrayListOf()
        columnMap.clear()
        classElementLookUpMap.clear()
        _primaryColumnDefinitions.clear()
        uniqueGroupsDefinitions.clear()
        indexGroupsDefinitions.clear()
        foreignKeyDefinitions.clear()
        columnMapDefinitions.clear()
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
            var uniqueNumbersSet: MutableSet<Int> = hashSetOf()
            for (uniqueGroup in groups) {
                if (uniqueNumbersSet.contains(uniqueGroup.groupNumber)) {
                    manager.logError("A duplicate unique group with number" +
                            " ${uniqueGroup.groupNumber} was found for $tableName")
                }
                val definition = UniqueGroupsDefinition(uniqueGroup)
                columnDefinitions.filter { it.uniqueGroups.contains(definition.number) }
                        .forEach { definition.addColumnDefinition(it) }
                uniqueGroupsDefinitions.add(definition)
                uniqueNumbersSet.add(uniqueGroup.groupNumber)
            }

            val indexGroups = table.indexGroups
            uniqueNumbersSet = hashSetOf()
            for (indexGroup in indexGroups) {
                if (uniqueNumbersSet.contains(indexGroup.number)) {
                    manager.logError(TableDefinition::class, "A duplicate unique index number" +
                            " ${indexGroup.number} was found for $elementName")
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
            val isColumnMap = element.annotation<ColumnMap>() != null
            if (element.annotation<Column>() != null || isForeign || isPrimary
                    || isAllFields || isInherited || isInheritedPrimaryKey || isColumnMap) {

                if (checkInheritancePackagePrivate(isPackagePrivateNotInSamePackage, element)) return

                val columnDefinition = if (isInheritedPrimaryKey) {
                    val inherited = inheritedPrimaryKeyMap[element.simpleName.toString()]
                    ColumnDefinition(manager, element, this, isPackagePrivateNotInSamePackage,
                            inherited?.column, inherited?.primaryKey)
                } else if (isInherited) {
                    val inherited = inheritedColumnMap[element.simpleName.toString()]
                    ColumnDefinition(manager, element, this, isPackagePrivateNotInSamePackage,
                            inherited?.column, null, inherited?.nonNullConflict
                            ?: ConflictAction.NONE)
                } else if (isForeign || isColumnMap) {
                    ReferenceColumnDefinition(manager, this,
                            element, isPackagePrivateNotInSamePackage)
                } else {
                    ColumnDefinition(manager, element,
                            this, isPackagePrivateNotInSamePackage)
                }

                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition)
                    columnMap.put(columnDefinition.columnName, columnDefinition)
                    // check to ensure not null.
                    when {
                        columnDefinition.isPrimaryKey -> _primaryColumnDefinitions.add(columnDefinition)
                        columnDefinition.isPrimaryKeyAutoIncrement -> {
                            autoIncrementColumn = columnDefinition
                            hasAutoIncrement = true
                        }
                        columnDefinition.isRowId -> {
                            autoIncrementColumn = columnDefinition
                            hasRowID = true
                        }
                    }

                    autoIncrementColumn?.let {
                        // check to ensure not null.
                        if (it.isNullableType) {
                            manager.logWarning("Attempting to use nullable field type on an autoincrementing column. " +
                                    "To suppress or remove this warning " +
                                    "switch to java primitive, add @android.support.annotation.NonNull," +
                                    "@org.jetbrains.annotation.NotNull, or in Kotlin don't make it nullable. Check the column ${it.columnName} " +
                                    "on $tableName")
                        }
                    }

                    if (columnDefinition is ReferenceColumnDefinition) {
                        if (!columnDefinition.isColumnMap) {
                            foreignKeyDefinitions.add(columnDefinition)
                        } else if (columnDefinition.isColumnMap) {
                            columnMapDefinitions.add(columnDefinition)
                        }
                    }

                    if (!columnDefinition.uniqueGroups.isEmpty()) {
                        val groups = columnDefinition.uniqueGroups
                        for (group in groups) {
                            columnUniqueMap.getOrPut(group) { mutableSetOf() }
                                    .add(columnDefinition)
                        }
                    }

                    if (isPackagePrivate) {
                        packagePrivateList.add(columnDefinition)
                    }
                }
            } else if (element.annotation<OneToMany>() != null) {
                val oneToManyDefinition = OneToManyDefinition(element as ExecutableElement, manager, elements)
                if (oneToManyValidator.validate(manager, oneToManyDefinition)) {
                    oneToManyDefinitions.add(oneToManyDefinition)
                }
            } else if (element.annotation<ModelCacheField>() != null) {
                ensureVisibleStatic(element, typeElement, "ModelCacheField")
                if (!customCacheFieldName.isNullOrEmpty()) {
                    manager.logError("ModelCacheField can only be declared once from: $typeElement")
                } else {
                    customCacheFieldName = element.simpleName.toString()
                }
            } else if (element.annotation<MultiCacheField>() != null) {
                ensureVisibleStatic(element, typeElement, "MultiCacheField")
                if (!customMultiCacheFieldName.isNullOrEmpty()) {
                    manager.logError("MultiCacheField can only be declared once from: $typeElement")
                } else {
                    customMultiCacheFieldName = element.simpleName.toString()
                }
            }
        }

        // ignore any referenced one to many field definitions from all fields.
        columnDefinitions = columnDefinitions.filterTo(mutableListOf()) { column ->
            oneToManyDefinitions.isEmpty() || oneToManyDefinitions.all { it.variableName != column.elementName }
        }
    }

    override val primaryColumnDefinitions: List<ColumnDefinition>
        get() = autoIncrementColumn?.let { arrayListOf(it) } ?: _primaryColumnDefinitions

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(com.dbflow5.processor.ClassNames.MODEL_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        // check references to properly set them up.
        foreignKeyDefinitions.forEach { it.checkNeedsReferences() }
        columnMapDefinitions.forEach { it.checkNeedsReferences() }
        typeBuilder.apply {

            writeGetModelClass(this, elementClassName)
            this.writeConstructor()

            `override fun`(String::class, "getTableName") {
                modifiers(public, final)
                `return`(tableName.quote().S)
            }

            if (updateConflictActionName.isNotEmpty()) {
                `override fun`(com.dbflow5.processor.ClassNames.CONFLICT_ACTION, "getUpdateOnConflictAction") {
                    modifiers(public, final)
                    `return`("\$T.$updateConflictActionName", com.dbflow5.processor.ClassNames.CONFLICT_ACTION)
                }
            }

            if (insertConflictActionName.isNotEmpty()) {
                `override fun`(com.dbflow5.processor.ClassNames.CONFLICT_ACTION, "getInsertOnConflictAction") {
                    modifiers(public, final)
                    `return`("\$T.$insertConflictActionName", com.dbflow5.processor.ClassNames.CONFLICT_ACTION)
                }
            }

            val paramColumnName = "columnName"
            val getPropertiesBuilder = CodeBlock.builder()

            `override fun`(com.dbflow5.processor.ClassNames.PROPERTY, "getProperty",
                    param(String::class, paramColumnName)) {
                modifiers(public, final)
                statement("$paramColumnName = \$T.quoteIfNeeded($paramColumnName)", com.dbflow5.processor.ClassNames.STRING_UTILS)

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

            `public static final field`(ArrayTypeName.of(com.dbflow5.processor.ClassNames.IPROPERTY), "ALL_COLUMN_PROPERTIES") {
                `=`("new \$T[]{\$L}", com.dbflow5.processor.ClassNames.IPROPERTY, getPropertiesBuilder.build().toString())
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
                }
            }

            val saveForeignKeyFields = columnDefinitions
                    .filter { (it is ReferenceColumnDefinition) && it.saveForeignKeyModel }
                    .map { it as ReferenceColumnDefinition }
            if (saveForeignKeyFields.isNotEmpty()) {
                val code = CodeBlock.builder()
                saveForeignKeyFields.forEach { it.appendSaveMethod(code) }

                `override fun`(TypeName.VOID, "saveForeignKeys", param(elementClassName!!, ModelUtils.variable),
                        param(com.dbflow5.processor.ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)) {
                    modifiers(public, final)
                    addCode(code.build())
                }
            }

            val deleteForeignKeyFields = columnDefinitions
                    .filter { (it is ReferenceColumnDefinition) && it.deleteForeignKeyModel }
                    .map { it as ReferenceColumnDefinition }
            if (deleteForeignKeyFields.isNotEmpty()) {
                val code = CodeBlock.builder()
                deleteForeignKeyFields.forEach { it.appendDeleteMethod(code) }

                `override fun`(TypeName.VOID, "deleteForeignKeys", param(elementClassName!!, ModelUtils.variable),
                        param(com.dbflow5.processor.ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)) {
                    modifiers(public, final)
                    addCode(code.build())
                }
            }

            `override fun`(ArrayTypeName.of(com.dbflow5.processor.ClassNames.IPROPERTY), "getAllColumnProperties") {
                modifiers(public, final)
                `return`("ALL_COLUMN_PROPERTIES")
            }

            if (!createWithDatabase) {
                `override fun`(TypeName.BOOLEAN, "createWithDatabase") {
                    modifiers(public, final)
                    `return`(false.L)
                }
            }

            if (cachingEnabled) {
                `public static final field`(com.dbflow5.processor.ClassNames.CACHE_ADAPTER, "cacheAdapter") {
                    `=` {
                        val primaryColumns = primaryColumnDefinitions

                        val hasCustomField = customCacheFieldName.isNotNullOrEmpty()
                        val hasCustomMultiCacheField = customMultiCacheFieldName.isNotNullOrEmpty()
                        var typeArgumentsString = ""
                        val typeClasses = mutableListOf<Any?>()
                        typeArgumentsString = if (hasCustomField) {
                            typeClasses += elementClassName
                            "\$T.$customCacheFieldName"
                        } else {
                            typeClasses += ClassNames.SIMPLE_MAP_CACHE
                            "new \$T($customCacheSize)"
                        }
                        typeArgumentsString += ", ${primaryColumns.size.L}"
                        typeArgumentsString += if (hasCustomMultiCacheField) {
                            typeClasses += elementClassName
                            ", \$T.$customMultiCacheFieldName"
                        } else {
                            ", null"
                        }
                        add("\$L",
                                TypeSpec.anonymousClassBuilder(typeArgumentsString, *typeClasses.toTypedArray())
                                        .addSuperinterface(ParameterizedTypeName.get(com.dbflow5.processor.ClassNames.CACHE_ADAPTER, elementTypeName))
                                        .apply {
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
                                                        param(com.dbflow5.processor.ClassNames.FLOW_CURSOR, "cursor")) {
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

                                                `override fun`(Any::class, "getCachingColumnValueFromCursor", param(com.dbflow5.processor.ClassNames.FLOW_CURSOR, "cursor")) {
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

                                            if (foreignKeyDefinitions.isNotEmpty()) {
                                                `override fun`(TypeName.VOID, "reloadRelationships",
                                                        param(elementClassName!!, ModelUtils.variable),
                                                        param(com.dbflow5.processor.ClassNames.FLOW_CURSOR, LoadFromCursorMethod.PARAM_CURSOR),
                                                        param(com.dbflow5.processor.ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)) {
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
                                        .build())
                    }
                }

                val singlePrimaryKey = primaryColumnDefinitions.size == 1

                `override fun`(com.dbflow5.processor.ClassNames.SINGLE_MODEL_LOADER, "createSingleModelLoader") {
                    modifiers(public, final)
                    addStatement("return new \$T<>(getTable(), cacheAdapter)",
                            if (singlePrimaryKey)
                                com.dbflow5.processor.ClassNames.SINGLE_KEY_CACHEABLE_MODEL_LOADER
                            else
                                com.dbflow5.processor.ClassNames.CACHEABLE_MODEL_LOADER)
                }
                `override fun`(com.dbflow5.processor.ClassNames.LIST_MODEL_LOADER, "createListModelLoader") {
                    modifiers(public, final)
                    `return`("new \$T<>(getTable(), cacheAdapter)",
                            if (singlePrimaryKey)
                                com.dbflow5.processor.ClassNames.SINGLE_KEY_CACHEABLE_LIST_MODEL_LOADER
                            else
                                com.dbflow5.processor.ClassNames.CACHEABLE_LIST_MODEL_LOADER)
                }
                `override fun`(ParameterizedTypeName.get(com.dbflow5.processor.ClassNames.CACHEABLE_LIST_MODEL_SAVER, elementClassName),
                        "createListModelSaver") {
                    modifiers(protected)
                    `return`("new \$T<>(getModelSaver(), cacheAdapter)", com.dbflow5.processor.ClassNames.CACHEABLE_LIST_MODEL_SAVER)
                }
                `override fun`(TypeName.BOOLEAN, "cachingEnabled") {
                    modifiers(public, final)
                    `return`(true.L)
                }

                `override fun`(elementClassName!!, "load", param(elementClassName!!, "model"),
                        param(com.dbflow5.processor.ClassNames.DATABASE_WRAPPER, wrapper)) {
                    modifiers(public, final)
                    statement("\$T loaded = super.load(model, $wrapper)", elementClassName!!)
                    statement("cacheAdapter.storeModelInCache(model)")
                    `return`("loaded")
                }

            }
        }

        methods.mapNotNull { it.methodSpec }
                .forEach { typeBuilder.addMethod(it) }
        if (generateContentValues) {
            contentValueMethods.mapNotNull { it.methodSpec }
                    .forEach { typeBuilder.addMethod(it) }
        }
    }
}
