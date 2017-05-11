package com.raizlabs.android.dbflow.processor.definition.column

import com.grosner.kpoet.S
import com.grosner.kpoet.`return`
import com.grosner.kpoet.case
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.utils.*
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.MirroredTypeException

/**
 * Description:
 */
class ForeignKeyColumnDefinition(manager: ProcessorManager, tableDefinition: TableDefinition,
                                 element: Element, isPackagePrivate: Boolean)
    : ColumnDefinition(manager, element, tableDefinition, isPackagePrivate) {

    val _foreignKeyReferenceDefinitionList: MutableList<ForeignKeyReferenceDefinition> = ArrayList()

    var referencedTableClassName: ClassName? = null

    var onDelete = ForeignKeyAction.NO_ACTION
    var onUpdate = ForeignKeyAction.NO_ACTION

    var isStubbedRelationship: Boolean = false

    var isReferencingTableObject: Boolean = false
    var implementsModel = false
    var extendsBaseModel = false

    var references: List<ForeignKeyReference>? = null

    var nonModelColumn: Boolean = false

    var saveForeignKeyModel: Boolean = false
    var deleteForeignKeyModel: Boolean = false

    var needsReferences = true

    var deferred = false

    override val typeConverterElementNames: List<TypeName?>
        get() = _foreignKeyReferenceDefinitionList.filter { it.hasTypeConverter }.map { it.columnClassName }

    init {

        element.annotation<ForeignKey>()?.let { foreignKey ->
            onUpdate = foreignKey.onUpdate
            onDelete = foreignKey.onDelete

            deferred = foreignKey.deferred
            isStubbedRelationship = foreignKey.stubbedRelationship

            try {
                foreignKey.tableClass
            } catch (mte: MirroredTypeException) {
                referencedTableClassName = fromTypeMirror(mte.typeMirror, manager)
            }

            val erasedElement = element.toTypeErasedElement()

            // hopefully intentionally left blank
            if (referencedTableClassName == TypeName.OBJECT) {
                if (elementTypeName is ParameterizedTypeName) {
                    val args = (elementTypeName as ParameterizedTypeName).typeArguments
                    if (args.size > 0) {
                        referencedTableClassName = ClassName.get(args[0].toTypeElement(manager))
                    }
                } else {
                    if (referencedTableClassName == null || referencedTableClassName == ClassName.OBJECT) {
                        referencedTableClassName = ClassName.get(elementTypeName.toTypeElement())
                    }
                }
            }

            if (referencedTableClassName == null) {
                manager.logError("Referenced was null for $element within $elementTypeName")
            }

            extendsBaseModel = erasedElement.isSubclass(manager.processingEnvironment, ClassNames.BASE_MODEL)
            implementsModel = erasedElement.implementsClass(manager.processingEnvironment, ClassNames.MODEL)
            isReferencingTableObject = implementsModel || erasedElement.annotation<Table>() != null

            nonModelColumn = !isReferencingTableObject

            saveForeignKeyModel = foreignKey.saveForeignKeyModel
            deleteForeignKeyModel = foreignKey.deleteForeignKeyModel

            references = foreignKey.references.asList()
        }
    }

    override fun addPropertyDefinition(typeBuilder: TypeSpec.Builder, tableClass: TypeName) {
        checkNeedsReferences()
        _foreignKeyReferenceDefinitionList.forEach {
            var propParam: TypeName? = null
            val colClassName = it.columnClassName
            colClassName?.let {
                propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, it.box())
            }
            if (it.columnName.isNullOrEmpty()) {
                manager.logError("Found empty reference name at ${it.foreignColumnName}" +
                    " from table ${baseTableDefinition.elementName}")
            }
            typeBuilder.addField(FieldSpec.builder(propParam, it.columnName,
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new \$T(\$T.class, \$S)", propParam, tableClass, it.columnName)
                .addJavadoc("Foreign Key" + if (isPrimaryKey) " / Primary Key" else "").build())
        }
    }

    override fun addPropertyCase(methodBuilder: MethodSpec.Builder) {
        checkNeedsReferences()
        _foreignKeyReferenceDefinitionList.forEach {
            methodBuilder.case(QueryBuilder.quoteIfNeeded(it.columnName).S) {
                `return`(it.columnName)
            }
        }
    }

    override fun appendIndexInitializer(initializer: CodeBlock.Builder, index: AtomicInteger) {
        if (nonModelColumn) {
            super.appendIndexInitializer(initializer, index)
        } else {
            checkNeedsReferences()
            _foreignKeyReferenceDefinitionList.forEach {
                if (index.get() > 0) {
                    initializer.add(", ")
                }
                initializer.add(it.columnName)
                index.incrementAndGet()
            }
        }
    }

    override fun addColumnName(codeBuilder: CodeBlock.Builder) {
        checkNeedsReferences()
        for (i in _foreignKeyReferenceDefinitionList.indices) {
            val reference = _foreignKeyReferenceDefinitionList[i]
            if (i > 0) {
                codeBuilder.add(",")
            }
            codeBuilder.add(reference.columnName)
        }
    }

    override val insertStatementColumnName: CodeBlock
        get() {
            checkNeedsReferences()
            val builder = CodeBlock.builder()
            _foreignKeyReferenceDefinitionList.indices.forEach { i ->
                if (i > 0) {
                    builder.add(",")
                }
                val referenceDefinition = _foreignKeyReferenceDefinitionList[i]
                builder.add(QueryBuilder.quote(referenceDefinition.columnName))
            }
            return builder.build()
        }

    override val insertStatementValuesString: CodeBlock
        get() {
            checkNeedsReferences()
            val builder = CodeBlock.builder()
            _foreignKeyReferenceDefinitionList.indices.forEach { i ->
                if (i > 0) {
                    builder.add(",")
                }
                builder.add("?")
            }
            return builder.build()
        }

    override val creationName: CodeBlock
        get() {
            checkNeedsReferences()
            val builder = CodeBlock.builder()
            _foreignKeyReferenceDefinitionList.indices.forEach { i ->
                if (i > 0) {
                    builder.add(" ,")
                }
                val referenceDefinition = _foreignKeyReferenceDefinitionList[i]
                builder.add(referenceDefinition.creationStatement)
            }
            return builder.build()
        }

    override val primaryKeyName: String
        get() {
            checkNeedsReferences()
            val builder = CodeBlock.builder()
            _foreignKeyReferenceDefinitionList.indices.forEach { i ->
                if (i > 0) {
                    builder.add(" ,")
                }
                val referenceDefinition = _foreignKeyReferenceDefinitionList[i]
                builder.add(referenceDefinition.primaryKeyName)
            }
            return builder.build().toString()
        }

    override val contentValuesStatement: CodeBlock
        get() = if (nonModelColumn) {
            super.contentValuesStatement
        } else {
            checkNeedsReferences()
            val codeBuilder = CodeBlock.builder()
            referencedTableClassName?.let {
                val foreignKeyCombiner = ForeignKeyAccessCombiner(columnAccessor)
                _foreignKeyReferenceDefinitionList.forEach {
                    foreignKeyCombiner.fieldAccesses += it.contentValuesField
                }
                foreignKeyCombiner.addCode(codeBuilder, AtomicInteger(0))
            }
            codeBuilder.build()
        }

    override fun getSQLiteStatementMethod(index: AtomicInteger): CodeBlock {
        if (nonModelColumn) {
            return super.getSQLiteStatementMethod(index)
        } else {
            checkNeedsReferences()
            val codeBuilder = CodeBlock.builder()
            referencedTableClassName?.let {
                val foreignKeyCombiner = ForeignKeyAccessCombiner(columnAccessor)
                _foreignKeyReferenceDefinitionList.forEach {
                    foreignKeyCombiner.fieldAccesses += it.sqliteStatementField
                }
                foreignKeyCombiner.addCode(codeBuilder, index)
            }
            return codeBuilder.build()
        }
    }

    override fun getLoadFromCursorMethod(endNonPrimitiveIf: Boolean, index: AtomicInteger, nameAllocator: NameAllocator): CodeBlock {
        if (nonModelColumn) {
            return super.getLoadFromCursorMethod(endNonPrimitiveIf, index, nameAllocator)
        } else {
            checkNeedsReferences()

            val code = CodeBlock.builder()
            referencedTableClassName?.let { referencedTableClassName ->

                val tableDefinition = manager.getTableDefinition(baseTableDefinition.databaseDefinition?.elementTypeName,
                    referencedTableClassName)
                val outputClassName = tableDefinition?.outputClassName
                outputClassName?.let {
                    val foreignKeyCombiner = ForeignKeyLoadFromCursorCombiner(columnAccessor,
                        referencedTableClassName, outputClassName, isStubbedRelationship, nameAllocator)
                    _foreignKeyReferenceDefinitionList.forEach {
                        foreignKeyCombiner.fieldAccesses += it.partialAccessor
                    }
                    foreignKeyCombiner.addCode(code, index)
                }
            }
            return code.build()
        }
    }

    override fun appendPropertyComparisonAccessStatement(codeBuilder: CodeBlock.Builder) {
        if (nonModelColumn || columnAccessor is TypeConverterScopeColumnAccessor) {
            super.appendPropertyComparisonAccessStatement(codeBuilder)
        } else {

            referencedTableClassName?.let {
                val foreignKeyCombiner = ForeignKeyAccessCombiner(columnAccessor)
                _foreignKeyReferenceDefinitionList.forEach {
                    foreignKeyCombiner.fieldAccesses += it.primaryReferenceField
                }
                foreignKeyCombiner.addCode(codeBuilder, AtomicInteger(0))
            }
        }
    }

    fun appendSaveMethod(codeBuilder: CodeBlock.Builder) {
        if (!nonModelColumn && columnAccessor !is TypeConverterScopeColumnAccessor) {
            referencedTableClassName?.let { referencedTableClassName ->
                val saveAccessor = ForeignKeyAccessField(columnName,
                    SaveModelAccessCombiner(Combiner(columnAccessor, referencedTableClassName, wrapperAccessor,
                        wrapperTypeName, subWrapperAccessor), implementsModel, extendsBaseModel))
                saveAccessor.addCode(codeBuilder, 0, modelBlock)
            }
        }
    }

    fun appendDeleteMethod(codeBuilder: CodeBlock.Builder) {
        if (!nonModelColumn && columnAccessor !is TypeConverterScopeColumnAccessor) {
            referencedTableClassName?.let { referencedTableClassName ->
                val deleteAccessor = ForeignKeyAccessField(columnName,
                    DeleteModelAccessCombiner(Combiner(columnAccessor, referencedTableClassName, wrapperAccessor,
                        wrapperTypeName, subWrapperAccessor), implementsModel, extendsBaseModel))
                deleteAccessor.addCode(codeBuilder, 0, modelBlock)
            }
        }
    }

    /**
     * If [ForeignKey] has no [ForeignKeyReference]s, we use the primary key the referenced
     * table. We do this post-evaluation so all of the [TableDefinition] can be generated.
     */
    private fun checkNeedsReferences() {
        val tableDefinition = (baseTableDefinition as TableDefinition)
        val referencedTableDefinition = manager.getTableDefinition(
            tableDefinition.databaseTypeName, referencedTableClassName)
        if (referencedTableDefinition == null) {
            manager.logError(ForeignKeyColumnDefinition::class,
                "Could not find the referenced table definition $referencedTableClassName" +
                    " from ${tableDefinition.tableName}. " +
                    "Ensure it exists in the samedatabase ${tableDefinition.databaseTypeName}")
        } else if (needsReferences) {
            val primaryColumns = referencedTableDefinition.primaryColumnDefinitions
            if (references?.isEmpty() ?: true) {
                primaryColumns.forEach {
                    val foreignKeyReferenceDefinition = ForeignKeyReferenceDefinition(manager,
                        elementName, it.elementName, it, this, primaryColumns.size)
                    _foreignKeyReferenceDefinitionList.add(foreignKeyReferenceDefinition)
                }
                needsReferences = false
            } else {
                references?.forEach { reference ->
                    val foundDefinition = primaryColumns.find { it.columnName == reference.foreignKeyColumnName }
                    if (foundDefinition == null) {
                        manager.logError(ForeignKeyColumnDefinition::class,
                            "Could not find referenced column ${reference.foreignKeyColumnName} " +
                                "from reference named ${reference.columnName}")
                    } else {
                        _foreignKeyReferenceDefinitionList.add(
                            ForeignKeyReferenceDefinition(manager, elementName,
                                foundDefinition.elementName, foundDefinition, this,
                                primaryColumns.size, reference.columnName))
                    }
                }
                needsReferences = false
            }

            if (nonModelColumn && _foreignKeyReferenceDefinitionList.size == 1) {
                columnName = _foreignKeyReferenceDefinitionList[0].columnName
            }
        }
    }
}
