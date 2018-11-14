package com.dbflow5.processor.definition

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.InheritedColumn
import com.dbflow5.annotation.InheritedPrimaryKey
import com.dbflow5.annotation.OneToMany
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.isNotNullOrEmpty
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ColumnValidator
import com.dbflow5.processor.OneToManyValidator
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.BindToStatementMethod.Mode.*
import com.dbflow5.processor.definition.behavior.AssociationalBehavior
import com.dbflow5.processor.definition.behavior.CachingBehavior
import com.dbflow5.processor.definition.behavior.CreationQueryBehavior
import com.dbflow5.processor.definition.behavior.CursorHandlingBehavior
import com.dbflow5.processor.definition.behavior.PrimaryKeyColumnBehavior
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.DefinitionUtils
import com.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.ModelUtils
import com.dbflow5.processor.utils.ModelUtils.wrapper
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
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
import java.util.concurrent.atomic.AtomicInteger
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Description: Used in writing ModelAdapters
 */
class TableDefinition(private val table: Table,
                      manager: ProcessorManager, element: TypeElement)
    : EntityDefinition(element, manager) {

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

    override val methods: Array<MethodDefinition> = arrayOf(
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
            OneToManySaveMethod(this, OneToManySaveMethod.METHOD_UPDATE))

    private val contentValueMethods: Array<MethodDefinition>

    private val creationQueryBehavior = CreationQueryBehavior(createWithDatabase = table.createWithDatabase)
    val useIsForPrivateBooleans: Boolean = table.useBooleanGetterSetters
    private val generateContentValues: Boolean = table.generateContentValues

    val oneToManyDefinitions = mutableListOf<OneToManyDefinition>()

    private val columnMap = mutableMapOf<String, ColumnDefinition>()
    private val columnUniqueMap = mutableMapOf<Int, MutableSet<ColumnDefinition>>()
    private val inheritedColumnMap = hashMapOf<String, InheritedColumn>()
    private val inheritedFieldNameList = mutableListOf<String>()
    private val inheritedPrimaryKeyMap = hashMapOf<String, InheritedPrimaryKey>()

    var hasPrimaryConstructor = false

    override val associationalBehavior = AssociationalBehavior(
            name = if (table.name.isNullOrEmpty()) element.simpleName.toString() else table.name,
            databaseTypeName = table.extractTypeNameFromAnnotation { it.database },
            allFields = table.allFields)

    override val cursorHandlingBehavior = CursorHandlingBehavior(
            orderedCursorLookup = table.orderedCursorLookUp,
            assignDefaultValuesFromCursor = table.assignDefaultValuesFromCursor)

    val cachingBehavior = CachingBehavior(
            cachingEnabled = table.cachingEnabled,
            customCacheSize = table.cacheSize,
            customCacheFieldName = null,
            customMultiCacheFieldName = null)

    init {
        setOutputClassName("_Table")

        manager.addModelToDatabase(elementClassName, associationalBehavior.databaseTypeName)

        val inheritedColumns = table.inheritedColumns
        inheritedColumns.forEach {
            if (inheritedFieldNameList.contains(it.fieldName)) {
                manager.logError("A duplicate inherited column with name %1s was found for %1s",
                        it.fieldName, associationalBehavior.name)
            }
            inheritedFieldNameList.add(it.fieldName)
            inheritedColumnMap[it.fieldName] = it
        }

        val inheritedPrimaryKeys = table.inheritedPrimaryKeys
        inheritedPrimaryKeys.forEach {
            if (inheritedFieldNameList.contains(it.fieldName)) {
                manager.logError("A duplicate inherited column with name %1s was found for %1s",
                        it.fieldName, associationalBehavior.name)
            }
            inheritedFieldNameList.add(it.fieldName)
            inheritedPrimaryKeyMap[it.fieldName] = it
        }

        implementsContentValuesListener = element.implementsClass(manager.processingEnvironment,
                ClassNames.CONTENT_VALUES_LISTENER)

        implementsSqlStatementListener = element.implementsClass(manager.processingEnvironment,
                ClassNames.SQLITE_STATEMENT_LISTENER)

        contentValueMethods = arrayOf(BindToContentValuesMethod(this, true, implementsContentValuesListener),
                BindToContentValuesMethod(this, false, implementsContentValuesListener))

    }

    override fun prepareForWriteInternal() {
        columnMap.clear()
        _primaryColumnDefinitions.clear()
        uniqueGroupsDefinitions.clear()
        indexGroupsDefinitions.clear()
        foreignKeyDefinitions.clear()
        columnMapDefinitions.clear()
        columnUniqueMap.clear()
        oneToManyDefinitions.clear()
        cachingBehavior.clear()

        // globular default
        var insertConflict = table.insertConflict
        if (insertConflict == ConflictAction.NONE && databaseDefinition.insertConflict != ConflictAction.NONE) {
            insertConflict = databaseDefinition.insertConflict
        }

        var updateConflict = table.updateConflict
        if (updateConflict == ConflictAction.NONE && databaseDefinition.updateConflict != ConflictAction.NONE) {
            updateConflict = databaseDefinition.updateConflict
        }

        val primaryKeyConflict = table.primaryKeyConflict

        insertConflictActionName = if (insertConflict == ConflictAction.NONE) "" else insertConflict.name
        updateConflictActionName = if (updateConflict == ConflictAction.NONE) "" else updateConflict.name
        primaryKeyConflictActionName = if (primaryKeyConflict == ConflictAction.NONE) "" else primaryKeyConflict.name

        typeElement?.let { createColumnDefinitions(it) }

        val groups = table.uniqueColumnGroups
        var uniqueNumbersSet: MutableSet<Int> = hashSetOf()
        for (uniqueGroup in groups) {
            if (uniqueNumbersSet.contains(uniqueGroup.groupNumber)) {
                manager.logError("A duplicate unique group with number" +
                        " ${uniqueGroup.groupNumber} was found for ${associationalBehavior.name}")
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

    override fun createColumnDefinitions(typeElement: TypeElement) {
        val elements = ElementUtility.getAllElements(typeElement, manager)

        for (element in elements) {
            classElementLookUpMap[element.simpleName.toString()] = element
            if (element is ExecutableElement && element.parameters.isEmpty()
                    && element.simpleName.toString() == "<init>"
                    && element.enclosingElement == typeElement
                    && !element.modifiers.contains(Modifier.PRIVATE)) {
                hasPrimaryConstructor = true
            }
        }

        if (!hasPrimaryConstructor) {
            manager.logError("For now, tables must have a visible, default, parameterless constructor. In" +
                    " Kotlin all field parameters must have default values.")
        }

        val columnValidator = ColumnValidator()
        val oneToManyValidator = OneToManyValidator()
        elements.forEach { variableElement ->
            // no private static or final fields for all columns, or any inherited columns here.
            val isAllFields = ElementUtility.isValidAllFields(associationalBehavior.allFields, variableElement)

            // package private, will generate helper
            val isPackagePrivate = ElementUtility.isPackagePrivate(variableElement)
            val isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, variableElement, this.element)

            val isForeign = variableElement.annotation<ForeignKey>() != null
            val isPrimary = variableElement.annotation<PrimaryKey>() != null
            val isInherited = inheritedColumnMap.containsKey(variableElement.simpleName.toString())
            val isInheritedPrimaryKey = inheritedPrimaryKeyMap.containsKey(variableElement.simpleName.toString())
            val isColumnMap = variableElement.annotation<ColumnMap>() != null
            if (variableElement.annotation<Column>() != null || isForeign || isPrimary
                    || isAllFields || isInherited || isInheritedPrimaryKey || isColumnMap) {

                if (checkInheritancePackagePrivate(isPackagePrivateNotInSamePackage, variableElement)) return

                val columnDefinition = if (isInheritedPrimaryKey) {
                    val inherited = inheritedPrimaryKeyMap[variableElement.simpleName.toString()]
                    ColumnDefinition(manager, variableElement, this, isPackagePrivateNotInSamePackage,
                            inherited?.column, inherited?.primaryKey)
                } else if (isInherited) {
                    val inherited = inheritedColumnMap[variableElement.simpleName.toString()]
                    ColumnDefinition(manager, variableElement, this, isPackagePrivateNotInSamePackage,
                            inherited?.column, null, inherited?.nonNullConflict
                            ?: ConflictAction.NONE)
                } else if (isForeign) {
                    ReferenceColumnDefinition(variableElement.annotation<ForeignKey>()!!, manager, this,
                            variableElement, isPackagePrivateNotInSamePackage)
                } else if (isColumnMap) {
                    ReferenceColumnDefinition(variableElement.annotation<ColumnMap>()!!,
                            manager, this, variableElement, isPackagePrivateNotInSamePackage)
                } else {
                    ColumnDefinition(manager, variableElement,
                            this, isPackagePrivateNotInSamePackage)
                }

                if (columnValidator.validate(manager, columnDefinition)) {
                    columnDefinitions.add(columnDefinition)
                    if (isPackagePrivate) {
                        packagePrivateList.add(columnDefinition)
                    }
                    columnMap[columnDefinition.columnName] = columnDefinition
                    // check to ensure not null.
                    when {
                        columnDefinition.type is ColumnDefinition.Type.Primary ->
                            _primaryColumnDefinitions.add(columnDefinition)
                        columnDefinition.type is ColumnDefinition.Type.PrimaryAutoIncrement -> {
                            this.primaryKeyColumnBehavior = PrimaryKeyColumnBehavior(
                                    hasRowID = false,
                                    hasAutoIncrement = true,
                                    associatedColumn = columnDefinition
                            )
                        }
                        columnDefinition.type is ColumnDefinition.Type.RowId -> {
                            this.primaryKeyColumnBehavior = PrimaryKeyColumnBehavior(
                                    hasRowID = true,
                                    hasAutoIncrement = false,
                                    associatedColumn = columnDefinition
                            )
                        }
                    }

                    primaryKeyColumnBehavior.associatedColumn?.let {
                        // check to ensure not null.
                        if (it.isNullableType) {
                            manager.logWarning("Attempting to use nullable field type on an autoincrementing column. " +
                                    "To suppress or remove this warning " +
                                    "switch to java primitive, add @android.support.annotation.NonNull," +
                                    "@org.jetbrains.annotation.NotNull, or in Kotlin don't make it nullable. Check the column ${it.columnName} " +
                                    "on ${associationalBehavior.name}")
                        }
                    }

                    if (columnDefinition is ReferenceColumnDefinition) {
                        if (!columnDefinition.isColumnMap) {
                            foreignKeyDefinitions.add(columnDefinition)
                        } else {
                            columnMapDefinitions.add(columnDefinition)
                        }
                    }

                    if (!columnDefinition.uniqueGroups.isEmpty()) {
                        for (group in columnDefinition.uniqueGroups) {
                            columnUniqueMap.getOrPut(group) { mutableSetOf() }
                                    .add(columnDefinition)
                        }
                    }
                }
            } else if (variableElement.annotation<OneToMany>() != null) {
                val oneToManyDefinition = OneToManyDefinition(variableElement as ExecutableElement, manager, elements)
                if (oneToManyValidator.validate(manager, oneToManyDefinition)) {
                    oneToManyDefinitions.add(oneToManyDefinition)
                }
            } else {
                cachingBehavior.evaluateElement(variableElement, typeElement, manager)
            }
        }

        // ignore any referenced one to many field definitions from all fields.
        columnDefinitions = columnDefinitions.filterTo(mutableListOf()) { column ->
            oneToManyDefinitions.isEmpty() || oneToManyDefinitions.all { it.variableName != column.elementName }
        }
    }

    override val primaryColumnDefinitions: List<ColumnDefinition>
        get() = primaryKeyColumnBehavior.associatedColumn?.let { arrayListOf(it) }
                ?: _primaryColumnDefinitions

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        // check references to properly set them up.
        foreignKeyDefinitions.forEach { it.checkNeedsReferences() }
        columnMapDefinitions.forEach { it.checkNeedsReferences() }
        typeBuilder.apply {

            writeGetModelClass(this, elementClassName)
            this.writeConstructor()

            `override fun`(String::class, "getTableName") {
                modifiers(public, final)
                `return`(associationalBehavior.name.quote().S)
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
                statement("$paramColumnName = \$T.quoteIfNeeded($paramColumnName)", ClassNames.STRING_UTILS)

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

            if (primaryKeyColumnBehavior.hasAutoIncrement || primaryKeyColumnBehavior.hasRowID) {
                val autoIncrement = primaryKeyColumnBehavior.associatedColumn
                autoIncrement?.let {
                    `override fun`(TypeName.VOID, "updateAutoIncrement", param(elementClassName!!, ModelUtils.variable),
                            param(Number::class, "id")) {
                        modifiers(public, final)
                        addCode(autoIncrement.updateAutoIncrementMethod)
                    }
                }
            }

            val saveForeignKeyFields = columnDefinitions
                    .asSequence()
                    .filter { (it is ReferenceColumnDefinition) && it.foreignKeyColumnBehavior?.saveForeignKeyModel == true }
                    .map { it as ReferenceColumnDefinition }
                    .toList()
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
                    .asSequence()
                    .filter { (it is ReferenceColumnDefinition) && it.foreignKeyColumnBehavior?.deleteForeignKeyModel == true }
                    .map { it as ReferenceColumnDefinition }
                    .toList()
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

            creationQueryBehavior.addToType(this)

            if (cachingBehavior.cachingEnabled) {
                val (_, customCacheSize, customCacheFieldName, customMultiCacheFieldName) = cachingBehavior
                `public static final field`(ClassNames.CACHE_ADAPTER, "cacheAdapter") {
                    `=` {
                        val primaryColumns = primaryColumnDefinitions

                        val hasCustomField = customCacheFieldName.isNotNullOrEmpty()
                        val hasCustomMultiCacheField = customMultiCacheFieldName.isNotNullOrEmpty()
                        val typeClasses = mutableListOf<Any?>()
                        var typeArgumentsString = if (hasCustomField) {
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
                                        .addSuperinterface(ParameterizedTypeName.get(ClassNames.CACHE_ADAPTER, elementTypeName))
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
                                                        param(ClassNames.FLOW_CURSOR, "cursor")) {
                                                    modifiers(public, final)
                                                    for (i in primaryColumns.indices) {
                                                        val column = primaryColumns[i]
                                                        val method = DefinitionUtils.getLoadFromCursorMethodString(column.elementTypeName, column.complexColumnBehavior.wrapperTypeName)
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
                                                    val method = DefinitionUtils.getLoadFromCursorMethodString(column.elementTypeName, column.complexColumnBehavior.wrapperTypeName)
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
                                                        param(ClassNames.FLOW_CURSOR, LoadFromCursorMethod.PARAM_CURSOR),
                                                        param(ClassNames.DATABASE_WRAPPER, ModelUtils.wrapper)) {
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

                `override fun`(ClassNames.SINGLE_MODEL_LOADER, "createSingleModelLoader") {
                    modifiers(public, final)
                    addStatement("return new \$T<>(getTable(), cacheAdapter)",
                            if (singlePrimaryKey)
                                ClassNames.SINGLE_KEY_CACHEABLE_MODEL_LOADER
                            else
                                ClassNames.CACHEABLE_MODEL_LOADER)
                }
                `override fun`(ClassNames.LIST_MODEL_LOADER, "createListModelLoader") {
                    modifiers(public, final)
                    `return`("new \$T<>(getTable(), cacheAdapter)",
                            if (singlePrimaryKey)
                                ClassNames.SINGLE_KEY_CACHEABLE_LIST_MODEL_LOADER
                            else
                                ClassNames.CACHEABLE_LIST_MODEL_LOADER)
                }
                `override fun`(ParameterizedTypeName.get(ClassNames.CACHEABLE_LIST_MODEL_SAVER, elementClassName),
                        "createListModelSaver") {
                    modifiers(protected)
                    `return`("new \$T<>(getModelSaver(), cacheAdapter)", ClassNames.CACHEABLE_LIST_MODEL_SAVER)
                }
                `override fun`(TypeName.BOOLEAN, "cachingEnabled") {
                    modifiers(public, final)
                    `return`(true.L)
                }

                `override fun`(elementClassName!!, "load", param(elementClassName!!, "model"),
                        param(ClassNames.DATABASE_WRAPPER, wrapper)) {
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
