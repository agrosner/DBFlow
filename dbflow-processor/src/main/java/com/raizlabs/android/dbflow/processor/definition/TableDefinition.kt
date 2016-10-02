package com.raizlabs.android.dbflow.processor.definition

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorUtils
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.ForeignKeyColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.method.*
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.processor.validator.ColumnValidator
import com.raizlabs.android.dbflow.processor.validator.OneToManyValidator
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

    var _primaryColumnDefinitions: MutableList<ColumnDefinition>
    var foreignKeyDefinitions: MutableList<ForeignKeyColumnDefinition>
    var uniqueGroupsDefinitions: MutableList<UniqueGroupsDefinition>
    var indexGroupsDefinitions: MutableList<IndexGroupsDefinition>

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

    val mColumnMap: MutableMap<String, ColumnDefinition> = Maps.newHashMap<String, ColumnDefinition>()

    var columnUniqueMap: MutableMap<Int, MutableList<ColumnDefinition>>
            = Maps.newHashMap<Int, MutableList<ColumnDefinition>>()

    var oneToManyDefinitions: MutableList<OneToManyDefinition> = ArrayList()

    var inheritedColumnMap: MutableMap<String, InheritedColumn> = HashMap()
    var inheritedFieldNameList: MutableList<String> = ArrayList()
    var inheritedPrimaryKeyMap: MutableMap<String, InheritedPrimaryKey> = HashMap()

    init {

        _primaryColumnDefinitions = ArrayList<ColumnDefinition>()
        foreignKeyDefinitions = ArrayList<ForeignKeyColumnDefinition>()
        uniqueGroupsDefinitions = ArrayList<UniqueGroupsDefinition>()
        indexGroupsDefinitions = ArrayList<IndexGroupsDefinition>()

        val table = element.getAnnotation(Table::class.java)
        if (table != null) {
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

            implementsLoadFromCursorListener = ProcessorUtils.implementsClass(manager.processingEnvironment,
                    ClassNames.LOAD_FROM_CURSOR_LISTENER.toString(), element)

            implementsContentValuesListener = ProcessorUtils.implementsClass(manager.processingEnvironment,
                    ClassNames.CONTENT_VALUES_LISTENER.toString(), element)

            implementsSqlStatementListener = ProcessorUtils.implementsClass(manager.processingEnvironment,
                    ClassNames.SQLITE_STATEMENT_LISTENER.toString(), element)
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
        mColumnMap.clear()
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

                setOutputClassName(it.classSeparator + DBFLOW_TABLE_TAG)

                // globular default
                var insertConflict: ConflictAction? = table.insertConflict
                if (insertConflict == ConflictAction.NONE && it.insertConflict != ConflictAction.NONE) {
                    insertConflict = it.insertConflict
                }

                var updateConflict: ConflictAction? = table.updateConflict
                if (updateConflict == ConflictAction.NONE
                        && it.updateConflict != ConflictAction.NONE) {
                    updateConflict = it.updateConflict
                }

                val primaryKeyConflict = table.primaryKeyConflict

                insertConflictActionName = if (insertConflict == ConflictAction.NONE) "" else insertConflict?.name ?: ""
                updateConflictActionName = if (updateConflict == ConflictAction.NONE) "" else updateConflict?.name ?: ""
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
                for (columnDefinition in columnDefinitions) {
                    if (columnDefinition.uniqueGroups.contains(definition.number)) {
                        definition.addColumnDefinition(columnDefinition)
                    }
                }
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
                columnDefinitions.forEach {
                    if (it.indexGroups.contains(definition.indexNumber)) {
                        definition.columnDefinitionList.add(it)
                    }
                }
                indexGroupsDefinitions.add(definition)
                uniqueNumbersSet.add(indexGroup.number)
            }
        }

    }

    override fun createColumnDefinitions(typeElement: TypeElement) {
        val elements = ElementUtility.getAllElements(typeElement, manager)

        for (element in elements) {
            classElementLookUpMap.put(element.simpleName.toString(), element)
        }

        val columnValidator = ColumnValidator()
        val oneToManyValidator = OneToManyValidator()
        elements.forEach { element ->
            // no private static or final fields for all columns, or any inherited columns here.
            val isAllFields = ElementUtility.isValidAllFields(allFields, element)

            // package private, will generate helper
            val isPackagePrivate = ElementUtility.isPackagePrivate(element)
            val isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, element, this.element)

            val isForeign = element.getAnnotation(ForeignKey::class.java) != null
            val isPrimary = element.getAnnotation(PrimaryKey::class.java) != null
            val isInherited = inheritedColumnMap.containsKey(element.simpleName.toString())
            val isInheritedPrimaryKey = inheritedPrimaryKeyMap.containsKey(element.simpleName.toString())
            if (element.getAnnotation(Column::class.java) != null || isForeign || isPrimary
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
                    mColumnMap.put(columnDefinition.columnName, columnDefinition)
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
                            var groupList: MutableList<ColumnDefinition>? = columnUniqueMap[group]
                            if (groupList == null) {
                                groupList = ArrayList<ColumnDefinition>()
                                columnUniqueMap.put(group, groupList)
                            }
                            if (!groupList.contains(columnDefinition)) {
                                groupList.add(columnDefinition)
                            }
                        }
                    }

                    if (isPackagePrivate) {
                        packagePrivateList.add(columnDefinition)
                    }
                }
            } else if (element.getAnnotation(OneToMany::class.java) != null) {
                val oneToManyDefinition = OneToManyDefinition(element as ExecutableElement, manager)
                if (oneToManyValidator.validate(manager, oneToManyDefinition)) {
                    oneToManyDefinitions.add(oneToManyDefinition)
                }
            } else if (element.getAnnotation(ModelCacheField::class.java) != null) {
                if (!element.modifiers.contains(Modifier.PUBLIC)) {
                    manager.logError("ModelCacheField must be public from: " + typeElement)
                }
                if (!element.modifiers.contains(Modifier.STATIC)) {
                    manager.logError("ModelCacheField must be static from: " + typeElement)
                }
                if (!customCacheFieldName.isNullOrEmpty()) {
                    manager.logError("ModelCacheField can only be declared once from: " + typeElement)
                } else {
                    customCacheFieldName = element.simpleName.toString()
                }
            } else if (element.getAnnotation(MultiCacheField::class.java) != null) {
                if (!element.modifiers.contains(Modifier.PUBLIC)) {
                    manager.logError("MultiCacheField must be public from: " + typeElement)
                }
                if (!element.modifiers.contains(Modifier.STATIC)) {
                    manager.logError("MultiCacheField must be static from: " + typeElement)
                }
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

    override val propertyClassName: ClassName
        get() = outputClassName

    override val extendsClass: TypeName?
        get() = ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, elementClassName)

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {

        InternalAdapterHelper.writeGetModelClass(typeBuilder, elementClassName)
        InternalAdapterHelper.writeGetTableName(typeBuilder, tableName)

        val getAllColumnPropertiesMethod = FieldSpec.builder(
                ArrayTypeName.of(ClassNames.IPROPERTY), "ALL_COLUMN_PROPERTIES",
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        val getPropertiesBuilder = CodeBlock.builder()

        val paramColumnName = "columnName"
        val getPropertyForNameMethod = MethodSpec.methodBuilder("getProperty")
                .addAnnotation(Override::class.java)
                .addParameter(ClassName.get(String::class.java), paramColumnName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(ClassNames.BASE_PROPERTY)

        getPropertyForNameMethod.addStatement("\$L = \$T.quoteIfNeeded(\$L)", paramColumnName,
                ClassName.get(QueryBuilder::class.java), paramColumnName)

        getPropertyForNameMethod.beginControlFlow("switch (\$L) ", paramColumnName)
        columnDefinitions.indices.forEach { i ->
            if (i > 0) {
                getPropertiesBuilder.add(",")
            }
            val columnDefinition = columnDefinitions[i]
            elementClassName?.let { columnDefinition.addPropertyDefinition(typeBuilder, it) }
            columnDefinition.addPropertyCase(getPropertyForNameMethod)
            columnDefinition.addColumnName(getPropertiesBuilder)
        }
        getPropertyForNameMethod.beginControlFlow("default: ")
        getPropertyForNameMethod.addStatement("throw new \$T(\$S)", IllegalArgumentException::class.java,
                "Invalid column name passed. Ensure you are calling the correct table's column")
        getPropertyForNameMethod.endControlFlow()
        getPropertyForNameMethod.endControlFlow()

        getAllColumnPropertiesMethod.initializer("new \$T[]{\$L}", ClassNames.IPROPERTY, getPropertiesBuilder.build().toString())
        typeBuilder.addField(getAllColumnPropertiesMethod.build())

        // add index properties here
        for (indexGroupsDefinition in indexGroupsDefinitions) {
            typeBuilder.addField(indexGroupsDefinition.fieldSpec)
        }

        typeBuilder.addMethod(getPropertyForNameMethod.build())

        if (hasAutoIncrement || hasRowID) {
            val autoIncrement = autoIncrementColumn
            autoIncrement?.let {
                InternalAdapterHelper.writeUpdateAutoIncrement(typeBuilder, elementClassName, autoIncrement)

                typeBuilder.addMethod(MethodSpec.methodBuilder("getAutoIncrementingId")
                        .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addParameter(elementClassName, ModelUtils.variable)
                        .addStatement("return \$L", autoIncrement.getColumnAccessString(false))
                        .returns(ClassName.get(Number::class.java)).build())

                typeBuilder.addMethod(MethodSpec.methodBuilder("getAutoIncrementingColumnName")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addStatement("return \$S", QueryBuilder.stripQuotes(autoIncrement.columnName))
                        .returns(ClassName.get(String::class.java)).build())
            }
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("getAllColumnProperties")
                .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return ALL_COLUMN_PROPERTIES", outputClassName)
                .returns(ArrayTypeName.of(ClassNames.IPROPERTY)).build())

        if (cachingEnabled) {

            // TODO: pass in model cache loaders.

            val singlePrimaryKey = primaryColumnDefinitions.size == 1
            typeBuilder.addMethod(MethodSpec.methodBuilder("createSingleModelLoader")
                    .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return new \$T<>(getModelClass())",
                            if (singlePrimaryKey)
                                ClassNames.SINGLE_KEY_CACHEABLE_MODEL_LOADER
                            else
                                ClassNames.CACHEABLE_MODEL_LOADER).returns(ClassNames.SINGLE_MODEL_LOADER).build())

            typeBuilder.addMethod(MethodSpec.methodBuilder("createListModelLoader")
                    .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return new \$T<>(getModelClass())",
                            if (singlePrimaryKey)
                                ClassNames.SINGLE_KEY_CACHEABLE_LIST_MODEL_LOADER
                            else
                                ClassNames.CACHEABLE_LIST_MODEL_LOADER).returns(ClassNames.LIST_MODEL_LOADER).build())

            typeBuilder.addMethod(MethodSpec.methodBuilder("createListModelSaver")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return new \$T<>(getModelSaver())", ClassNames.CACHEABLE_LIST_MODEL_SAVER)
                    .returns(ParameterizedTypeName.get(ClassNames.CACHEABLE_LIST_MODEL_SAVER,
                            elementClassName)).build())

            typeBuilder.addMethod(MethodSpec.methodBuilder("cachingEnabled")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return \$L", true)
                    .returns(TypeName.BOOLEAN).build())

            val primaries = primaryColumnDefinitions
            InternalAdapterHelper.writeGetCachingId(typeBuilder, elementClassName, primaries)

            val cachingbuilder = MethodSpec.methodBuilder("createCachingColumns").addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            var columns = "return new String[]{"
            primaries.indices.forEach { i ->
                val column = primaries[i]
                if (i > 0) {
                    columns += ","
                }
                columns += "\"" + QueryBuilder.quoteIfNeeded(column.columnName) + "\""
            }
            columns += "}"

            cachingbuilder.addStatement(columns).returns(ArrayTypeName.of(ClassName.get(String::class.java)))

            typeBuilder.addMethod(cachingbuilder.build())

            if (cacheSize != Table.DEFAULT_CACHE_SIZE) {
                typeBuilder.addMethod(MethodSpec.methodBuilder("getCacheSize")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL).addStatement("return \$L", cacheSize)
                        .returns(TypeName.INT).build())
            }

            if (!customCacheFieldName.isNullOrEmpty()) {
                typeBuilder.addMethod(MethodSpec.methodBuilder("createModelCache")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addStatement("return \$T.\$L", elementClassName, customCacheFieldName)
                        .returns(ParameterizedTypeName.get(ClassNames.MODEL_CACHE, elementClassName,
                                WildcardTypeName.subtypeOf(Any::class.java))).build())
            }

            if (!customMultiCacheFieldName.isNullOrEmpty()) {
                typeBuilder.addMethod(MethodSpec.methodBuilder("getCacheConverter")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addStatement("return \$T.\$L", elementClassName, customMultiCacheFieldName)
                        .returns(ParameterizedTypeName.get(ClassNames.MULTI_KEY_CACHE_CONVERTER,
                                WildcardTypeName.subtypeOf(Any::class.java))).build())
            }

            val reloadMethod = MethodSpec.methodBuilder("reloadRelationships")
                    .addAnnotation(Override::class.java)
                    .addParameter(elementClassName, ModelUtils.variable)
                    .addParameter(ClassNames.CURSOR, LoadFromCursorMethod.PARAM_CURSOR)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            val loadStatements = CodeBlock.builder()
            val noIndex = AtomicInteger(-1)
            foreignKeyDefinitions.forEach {
                val codeBuilder = it.getLoadFromCursorMethod(false, noIndex).toBuilder()
                val typeName = it.elementTypeName
                if (typeName != null && !typeName.isPrimitive) {
                    codeBuilder.nextControlFlow("else")
                    codeBuilder.add(it.setColumnAccessString(CodeBlock.builder().add("null").build())
                            .toBuilder().add(";\n").build())
                    codeBuilder.endControlFlow()
                }
                loadStatements.add(codeBuilder.build())
            }
            reloadMethod.addCode(loadStatements.build())
            typeBuilder.addMethod(reloadMethod.build())
        }

        val customTypeConverterPropertyMethod = CustomTypeConverterPropertyMethod(this)
        customTypeConverterPropertyMethod.addToType(typeBuilder)

        val constructorCode = CodeBlock.builder()
        constructorCode.addStatement("super(databaseDefinition)")
        customTypeConverterPropertyMethod.addCode(constructorCode)

        typeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(ClassNames.DATABASE_HOLDER, "holder")
                .addParameter(ClassNames.BASE_DATABASE_DEFINITION_CLASSNAME, "databaseDefinition")
                .addCode(constructorCode.build()).addModifiers(Modifier.PUBLIC).build())

        for (methodDefinition in methods) {
            val spec = methodDefinition.methodSpec
            if (spec != null) {
                typeBuilder.addMethod(spec)
            }
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("newInstance")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return new \$T()", elementClassName)
                .returns(elementClassName).build())

        if (!updateConflictActionName.isEmpty()) {
            typeBuilder.addMethod(MethodSpec.methodBuilder("getUpdateOnConflictAction")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return \$T.\$L", ClassNames.CONFLICT_ACTION, updateConflictActionName)
                    .returns(ClassNames.CONFLICT_ACTION).build())
        }

        if (!insertConflictActionName.isEmpty()) {
            typeBuilder.addMethod(MethodSpec.methodBuilder("getInsertOnConflictAction")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return \$T.\$L", ClassNames.CONFLICT_ACTION, insertConflictActionName)
                    .returns(ClassNames.CONFLICT_ACTION).build())
        }
    }

    companion object {

        val DBFLOW_TABLE_TAG = "Table"
    }
}
