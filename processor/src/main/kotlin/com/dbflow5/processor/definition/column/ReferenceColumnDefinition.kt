package com.dbflow5.processor.definition.column

import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.ForeignKeyReference
import com.dbflow5.annotation.QueryModel
import com.dbflow5.annotation.Table
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ColumnValidator
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.EntityDefinition
import com.dbflow5.processor.definition.QueryModelDefinition
import com.dbflow5.processor.definition.TableDefinition
import com.dbflow5.processor.definition.behavior.ForeignKeyColumnBehavior
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.extractTypeMirrorFromAnnotation
import com.dbflow5.processor.utils.fromTypeMirror
import com.dbflow5.processor.utils.implementsClass
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.processor.utils.isSubclass
import com.dbflow5.processor.utils.toClassName
import com.dbflow5.processor.utils.toTypeElement
import com.dbflow5.processor.utils.toTypeErasedElement
import com.dbflow5.quote
import com.dbflow5.quoteIfNeeded
import com.grosner.kpoet.S
import com.grosner.kpoet.`return`
import com.grosner.kpoet.case
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.util.concurrent.atomic.AtomicInteger
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeMirror

/**
 * Description: Represents both a [ForeignKey] and [ColumnMap]. Builds up the model of fields
 * required to generate definitions.
 */
class ReferenceColumnDefinition
private constructor(manager: ProcessorManager, tableDefinition: EntityDefinition,
                    element: Element, isPackagePrivate: Boolean,
                    /**
                     * If null, its a [ColumnMap]
                     */
                    val foreignKeyColumnBehavior: ForeignKeyColumnBehavior?,

                    /**
                     * Foreign key references. If exists, it's precomputed first.
                     */
                    private var references: List<ReferenceSpecificationDefinition>,

                    /**
                     * If true, full model object does not load on cursor load.
                     */
                    private val isStubbedRelationship: Boolean)
    : ColumnDefinition(manager, element, tableDefinition, isPackagePrivate) {

    private val _referenceDefinitionList: MutableList<ReferenceDefinition> = arrayListOf()
    val referenceDefinitionList: List<ReferenceDefinition>
        get() {
            checkNeedsReferences()
            return _referenceDefinitionList
        }

    var referencedClassName: ClassName? = null

    var isReferencingTableObject: Boolean = false

    private var implementsModel = false
    private var extendsBaseModel = false
    private var nonModelColumn: Boolean = false

    val isColumnMap: Boolean
        get() = foreignKeyColumnBehavior == null

    private var needsReferences = true
    val explicitReferences = references.isNotEmpty()

    override val typeConverterElementNames: List<TypeName?>
        get() {
            val uniqueTypes = mutableSetOf<TypeName?>()
            referenceDefinitionList.filter { it.hasTypeConverter }.mapTo(uniqueTypes) { it.columnClassName }
            return uniqueTypes.toList()
        }

    constructor(columnMap: ColumnMap, manager: ProcessorManager, tableDefinition: EntityDefinition,
                element: Element, isPackagePrivate: Boolean) : this(
        manager = manager,
        tableDefinition = tableDefinition,
        element = element,
        isPackagePrivate = isPackagePrivate,
        foreignKeyColumnBehavior = null,
        references = columnMap.references.map { reference ->
            val typeMirror = reference.extractTypeMirrorFromAnnotation { it.typeConverter }
            val typeConverterClassName = typeMirror?.let { fromTypeMirror(typeMirror, manager) }
            ReferenceSpecificationDefinition(columnName = reference.columnName,
                referenceName = reference.columnMapFieldName,
                onNullConflictAction = reference.notNull.onNullConflict,
                defaultValue = reference.defaultValue,
                typeConverterClassName = typeConverterClassName,
                typeConverterTypeMirror = typeMirror)
        },
        // column map is always stubbed
        isStubbedRelationship = true
    ) {
        findReferencedClassName(manager)

        // self create a column map if defined here.
        typeElement?.let { typeElement ->
            QueryModelDefinition(typeElement, tableDefinition.associationalBehavior.databaseTypeName, manager).apply {
                manager.addQueryModelDefinition(this)
            }
        }
    }

    constructor(foreignKey: ForeignKey, manager: ProcessorManager, tableDefinition: EntityDefinition,
                element: Element, isPackagePrivate: Boolean) :
        this(
            manager = manager,
            tableDefinition = tableDefinition,
            element = element,
            isPackagePrivate = isPackagePrivate,
            foreignKeyColumnBehavior = ForeignKeyColumnBehavior(onDelete = foreignKey.onDelete, onUpdate = foreignKey.onUpdate,
                saveForeignKeyModel = foreignKey.saveForeignKeyModel,
                deleteForeignKeyModel = foreignKey.deleteForeignKeyModel,
                deferred = foreignKey.deferred),
            references = foreignKey.references.map { reference ->
                ReferenceSpecificationDefinition(columnName = reference.columnName,
                    referenceName = reference.foreignKeyColumnName,
                    onNullConflictAction = reference.notNull.onNullConflict,
                    defaultValue = reference.defaultValue)
            },
            isStubbedRelationship = foreignKey.stubbedRelationship
        ) {
        if (tableDefinition !is TableDefinition) {
            manager.logError("Class $elementName cannot declare a @ForeignKey. Use @ColumnMap instead.")
        }

        referencedClassName = foreignKey.extractTypeMirrorFromAnnotation { it.tableClass }
            ?.let { fromTypeMirror(it, manager) }

        // hopefully intentionally left blank
        if (referencedClassName == TypeName.OBJECT) {
            findReferencedClassName(manager)
        }

        if (referencedClassName == null) {
            manager.logError("Referenced was null for $element within $elementTypeName")
        }

        val erasedElement = element.toTypeErasedElement()
        extendsBaseModel = erasedElement.isSubclass(manager.processingEnvironment, ClassNames.BASE_MODEL)
        implementsModel = erasedElement.implementsClass(manager.processingEnvironment, ClassNames.MODEL)
        isReferencingTableObject = implementsModel || erasedElement.annotation<Table>() != null

        nonModelColumn = !isReferencingTableObject
    }

    init {
        if (isNotNullType) {
            manager.logError("Foreign Keys must be nullable. Please remove the non-null annotation if using " +
                "Java, or add ? to the type for Kotlin.")
        }
    }

    private fun findReferencedClassName(manager: ProcessorManager) {
        if (elementTypeName is ParameterizedTypeName) {
            val args = elementTypeName.typeArguments
            if (args.size > 0) {
                referencedClassName = ClassName.get(args[0].toTypeElement(manager))
            }
        } else {
            if (referencedClassName == null || referencedClassName == ClassName.OBJECT) {
                referencedClassName = elementTypeName.toTypeElement().toClassName()
            }
        }
    }

    override fun addPropertyDefinition(typeBuilder: TypeSpec.Builder, tableClass: TypeName) {
        referenceDefinitionList.forEach { referenceDefinition ->
            var propParam: TypeName? = null
            val colClassName = referenceDefinition.columnClassName
            colClassName?.let {
                propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, it.box())
            }
            if (referenceDefinition.columnName.isNullOrEmpty()) {
                manager.logError("Found empty reference name at ${referenceDefinition.foreignColumnName}" +
                    " from table ${entityDefinition.elementName}")
            }
            typeBuilder.addField(FieldSpec.builder(propParam, referenceDefinition.columnName,
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new \$T(\$T.class, \$S)", propParam, tableClass, referenceDefinition.columnName)
                .addJavadoc(
                    if (isColumnMap) "Column Mapped Field"
                    else ("Foreign Key${if (type == Type.Primary) " / Primary Key" else ""}"))
                .build())
        }
    }

    override fun addPropertyCase(methodBuilder: MethodSpec.Builder) {
        referenceDefinitionList.forEach {
            methodBuilder.case(it.columnName.quoteIfNeeded().S) {
                `return`(it.columnName)
            }
        }
    }

    override fun appendIndexInitializer(initializer: CodeBlock.Builder, index: AtomicInteger) {
        if (nonModelColumn) {
            super.appendIndexInitializer(initializer, index)
        } else {
            referenceDefinitionList.forEach {
                if (index.get() > 0) {
                    initializer.add(", ")
                }
                initializer.add(it.columnName)
                index.incrementAndGet()
            }
        }
    }

    override fun addColumnName(codeBuilder: CodeBlock.Builder) {
        referenceDefinitionList.withIndex().forEach { (i, reference) ->
            if (i > 0) {
                codeBuilder.add(",")
            }
            codeBuilder.add(reference.columnName)
        }
    }

    override val updateStatementBlock: CodeBlock
        get() {
            val builder = CodeBlock.builder()
            referenceDefinitionList.withIndex().forEach { (i, referenceDefinition) ->
                if (i > 0) {
                    builder.add(",")
                }
                builder.add(CodeBlock.of("${referenceDefinition.columnName.quote()}=?"))
            }
            return builder.build()
        }

    override val insertStatementColumnName: CodeBlock
        get() {
            val builder = CodeBlock.builder()
            referenceDefinitionList.withIndex().forEach { (i, referenceDefinition) ->
                if (i > 0) {
                    builder.add(",")
                }
                builder.add(referenceDefinition.columnName.quote())
            }
            return builder.build()
        }

    override val insertStatementValuesString: CodeBlock
        get() {
            val builder = CodeBlock.builder()
            referenceDefinitionList.indices.forEach { i ->
                if (i > 0) {
                    builder.add(",")
                }
                builder.add("?")
            }
            return builder.build()
        }

    override val creationName: CodeBlock
        get() {
            val builder = CodeBlock.builder()
            referenceDefinitionList.withIndex().forEach { (i, referenceDefinition) ->
                if (i > 0) {
                    builder.add(" ,")
                }
                builder.add(referenceDefinition.creationStatement)

                if (referenceDefinition.notNull) {
                    builder.add(" NOT NULL ON CONFLICT \$L", referenceDefinition.onNullConflict)
                } else if (!explicitReferences && notNull) {
                    builder.add(" NOT NULL ON CONFLICT \$L", onNullConflict)
                }
            }
            return builder.build()
        }

    override val primaryKeyName: String
        get() {
            val builder = CodeBlock.builder()
            referenceDefinitionList.withIndex().forEach { (i, referenceDefinition) ->
                if (i > 0) {
                    builder.add(" ,")
                }
                builder.add(referenceDefinition.primaryKeyName)
            }
            return builder.build().toString()
        }

    override val contentValuesStatement: CodeBlock
        get() = if (nonModelColumn) {
            super.contentValuesStatement
        } else {
            val codeBuilder = CodeBlock.builder()
            referencedClassName?.let { _ ->
                val foreignKeyCombiner = ForeignKeyAccessCombiner(columnAccessor)
                referenceDefinitionList.forEach {
                    foreignKeyCombiner.fieldAccesses += it.contentValuesField
                }
                foreignKeyCombiner.addCode(codeBuilder, AtomicInteger(0))
            }
            codeBuilder.build()
        }

    override fun getSQLiteStatementMethod(index: AtomicInteger, defineProperty: Boolean): CodeBlock {
        if (nonModelColumn) {
            return super.getSQLiteStatementMethod(index, defineProperty)
        } else {
            val codeBuilder = CodeBlock.builder()
            referencedClassName?.let {
                val foreignKeyCombiner = ForeignKeyAccessCombiner(columnAccessor)
                referenceDefinitionList.forEach {
                    foreignKeyCombiner.fieldAccesses += it.sqliteStatementField
                }
                foreignKeyCombiner.addCode(codeBuilder, index, defineProperty)
            }
            return codeBuilder.build()
        }
    }

    override fun getLoadFromCursorMethod(endNonPrimitiveIf: Boolean, index: AtomicInteger, nameAllocator: NameAllocator): CodeBlock {
        if (nonModelColumn) {
            return super.getLoadFromCursorMethod(endNonPrimitiveIf, index, nameAllocator)
        } else {
            val code = CodeBlock.builder()
            referencedClassName?.let { referencedTableClassName ->

                val tableDefinition = manager.getReferenceDefinition(
                    entityDefinition.databaseDefinition.elementTypeName, referencedTableClassName)
                tableDefinition?.outputClassName?.let { outputClassName ->
                    val foreignKeyCombiner = ForeignKeyLoadFromCursorCombiner(columnAccessor,
                        referencedTableClassName, outputClassName, isStubbedRelationship, nameAllocator)
                    referenceDefinitionList.forEach {
                        foreignKeyCombiner.fieldAccesses += it.partialAccessor
                    }
                    foreignKeyCombiner.addCode(code, index)
                }
            }
            return code.build()
        }
    }

    override fun appendPropertyComparisonAccessStatement(codeBuilder: CodeBlock.Builder) {
        when {
            nonModelColumn -> PrimaryReferenceAccessCombiner(combiner).apply {
                codeBuilder.addCode(referenceDefinitionList[0].columnName, getDefaultValueBlock(), 0, modelBlock)
            }
            columnAccessor is TypeConverterScopeColumnAccessor -> super.appendPropertyComparisonAccessStatement(codeBuilder)
            else -> referencedClassName?.let { _ ->
                val foreignKeyCombiner = ForeignKeyAccessCombiner(columnAccessor)
                referenceDefinitionList.forEach {
                    foreignKeyCombiner.fieldAccesses += it.primaryReferenceField
                }
                foreignKeyCombiner.addCode(codeBuilder, AtomicInteger(0))
            }
        }
    }

    fun appendSaveMethod(codeBuilder: CodeBlock.Builder) {
        if (!nonModelColumn && columnAccessor !is TypeConverterScopeColumnAccessor) {
            referencedClassName?.let { referencedTableClassName ->
                val saveAccessor = ForeignKeyAccessField(columnName,
                    SaveModelAccessCombiner(Combiner(columnAccessor, referencedTableClassName,
                        complexColumnBehavior.wrapperAccessor,
                        complexColumnBehavior.wrapperTypeName,
                        complexColumnBehavior.subWrapperAccessor), implementsModel, extendsBaseModel))
                saveAccessor.addCode(codeBuilder, 0, modelBlock)
            }
        }
    }

    fun appendDeleteMethod(codeBuilder: CodeBlock.Builder) {
        if (!nonModelColumn && columnAccessor !is TypeConverterScopeColumnAccessor) {
            referencedClassName?.let { referencedTableClassName ->
                val deleteAccessor = ForeignKeyAccessField(columnName,
                    DeleteModelAccessCombiner(Combiner(columnAccessor, referencedTableClassName,
                        complexColumnBehavior.wrapperAccessor,
                        complexColumnBehavior.wrapperTypeName,
                        complexColumnBehavior.subWrapperAccessor), implementsModel, extendsBaseModel))
                deleteAccessor.addCode(codeBuilder, 0, modelBlock)
            }
        }
    }

    /**
     * If [ForeignKey] has no [ForeignKeyReference]s, we use the primary key the referenced
     * table. We do this post-evaluation so all of the [TableDefinition] can be generated.
     */
    fun checkNeedsReferences() {
        val referencedTableDefinition = manager.getReferenceDefinition(entityDefinition.associationalBehavior.databaseTypeName, referencedClassName)
        if (referencedTableDefinition == null) {
            throwCannotFindReference()
        } else if (needsReferences) {
            val primaryColumns =
                if (isColumnMap) referencedTableDefinition.columnDefinitions
                else referencedTableDefinition.primaryColumnDefinitions
            if (references.isEmpty()) {
                primaryColumns.forEach { columnDefinition ->
                    val typeMirror = columnDefinition.column?.extractTypeMirrorFromAnnotation { it.typeConverter }
                    val typeConverterClassName = typeMirror?.let { fromTypeMirror(typeMirror, manager) }
                    val referenceDefinition = ReferenceDefinition(manager,
                        foreignKeyFieldName = elementName,
                        foreignKeyElementName = columnDefinition.elementName,
                        referencedColumn = columnDefinition,
                        referenceColumnDefinition = this,
                        referenceCount = primaryColumns.size,
                        localColumnName = if (isColumnMap) columnDefinition.elementName else "",
                        defaultValue = columnDefinition.defaultValue,
                        typeConverterClassName = typeConverterClassName,
                        typeConverterTypeMirror = typeMirror
                    )
                    _referenceDefinitionList.add(referenceDefinition)
                }
                needsReferences = false
            } else {
                references.forEach { reference ->
                    val foundDefinition = primaryColumns.find { it.columnName == reference.referenceName }
                    if (foundDefinition == null) {
                        manager.logError(ReferenceColumnDefinition::class,
                            "Could not find referenced column ${reference.referenceName} " +
                                "from reference named ${reference.columnName}")
                    } else {
                        _referenceDefinitionList.add(
                            ReferenceDefinition(manager,
                                foreignKeyFieldName = elementName,
                                foreignKeyElementName = foundDefinition.elementName,
                                referencedColumn = foundDefinition,
                                referenceColumnDefinition = this,
                                referenceCount = primaryColumns.size,
                                localColumnName = reference.columnName,
                                onNullConflict = reference.onNullConflictAction,
                                defaultValue = reference.defaultValue,
                                typeConverterClassName = reference.typeConverterClassName,
                                typeConverterTypeMirror = reference.typeConverterTypeMirror
                            ))
                    }
                }
                needsReferences = false
            }

            if (nonModelColumn && _referenceDefinitionList.size == 1) {
                columnName = _referenceDefinitionList[0].columnName
            }

            _referenceDefinitionList.forEach {
                if (it.columnClassName?.isPrimitive == true
                    && !it.defaultValue.isNullOrEmpty()) {
                    manager.logWarning(ColumnValidator::class.java,
                        "Default value of \"${it.defaultValue}\" from " +
                            "${entityDefinition.elementName}.$elementName is ignored for primitive columns.")
                }
            }
        }
    }

    fun throwCannotFindReference() {
        manager.logError(ReferenceColumnDefinition::class,
            "Could not find the referenced ${Table::class.java.simpleName} " +
                "or ${QueryModel::class.java.simpleName} definition $referencedClassName" +
                " from ${entityDefinition.elementName}. " +
                "Ensure it exists in the same database as ${entityDefinition.associationalBehavior.databaseTypeName}")
    }
}

/**
 * Description: defines a ForeignKeyReference or ColumnMapReference.
 */
class ReferenceSpecificationDefinition(val columnName: String,
                                       val referenceName: String,
                                       val onNullConflictAction: ConflictAction,
                                       val defaultValue: String,
                                       val typeConverterClassName: ClassName? = null,
                                       val typeConverterTypeMirror: TypeMirror? = null)