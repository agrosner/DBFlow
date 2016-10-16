package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.ProcessorUtils
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.utils.capitalizeFirstLetter
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * Description:
 */
class ForeignKeyColumnDefinition(manager: ProcessorManager, tableDefinition: TableDefinition,
                                 typeElement: Element, isPackagePrivate: Boolean)
: ColumnDefinition(manager, typeElement, tableDefinition, isPackagePrivate) {

    val _foreignKeyReferenceDefinitionList: MutableList<ForeignKeyReferenceDefinition> = ArrayList()

    var referencedTableClassName: ClassName? = null

    var onDelete: ForeignKeyAction
    var onUpdate: ForeignKeyAction

    var isStubbedRelationship: Boolean = false

    var isModel: Boolean = false

    var needsReferences: Boolean = false

    var nonModelColumn: Boolean = false

    var saveForeignKeyModel: Boolean = false


    override val typeConverterElementNames: List<TypeName?>
        get() = _foreignKeyReferenceDefinitionList.filter { it.hasTypeConverter }.map { it.columnClassName }

    init {

        val foreignKey = typeElement.getAnnotation(ForeignKey::class.java)
        onUpdate = foreignKey.onUpdate
        onDelete = foreignKey.onDelete

        isStubbedRelationship = foreignKey.stubbedRelationship

        try {
            foreignKey.tableClass
        } catch (mte: MirroredTypeException) {
            referencedTableClassName = ProcessorUtils.fromTypeMirror(mte.typeMirror, manager)
        }

        val erasedElement: TypeElement? = manager.elements.getTypeElement(
                manager.typeUtils.erasure(typeElement.asType()).toString())

        // hopefully intentionally left blank
        if (referencedTableClassName == TypeName.OBJECT) {
            if (elementTypeName is ParameterizedTypeName) {
                val args = (elementTypeName as ParameterizedTypeName).typeArguments
                if (args.size > 0) {
                    referencedTableClassName = ClassName.get(manager.elements.getTypeElement(args[0].toString()))
                }
            } else {
                if (referencedTableClassName == null || referencedTableClassName == ClassName.OBJECT) {
                    referencedTableClassName = ClassName.get(manager.elements.getTypeElement(elementTypeName.toString()))
                }
            }
        }

        if (referencedTableClassName == null) {
            manager.logError("Referenced was null for %1s within %1s", typeElement, elementTypeName)
        }

        isModel = ProcessorUtils.implementsClass(manager.processingEnvironment, ClassNames.MODEL.toString(), erasedElement)
        isModel = isModel || erasedElement?.getAnnotation(Table::class.java) != null

        nonModelColumn = !isModel

        saveForeignKeyModel = foreignKey.saveForeignKeyModel

        val references = foreignKey.references
        if (references.size == 0) {
            // no references specified we will delegate references call to post-evaluation
            needsReferences = true
        } else {
            for (reference in references) {
                val referenceDefinition = ForeignKeyReferenceDefinition(manager, elementName, reference, this)
                // TODO: add validation
                _foreignKeyReferenceDefinitionList.add(referenceDefinition)
            }

            if (nonModelColumn && _foreignKeyReferenceDefinitionList.size == 1) {
                val foreignKeyReferenceDefinition = _foreignKeyReferenceDefinitionList[0]
                columnName = foreignKeyReferenceDefinition.columnName
            }
        }
    }

    override fun addPropertyDefinition(typeBuilder: TypeSpec.Builder, tableClass: TypeName) {
        checkNeedsReferences()
        _foreignKeyReferenceDefinitionList.forEach {
            var propParam: TypeName? = null
            val colClassName = it.columnClassName
            colClassName?.let {
                if (it.isPrimitive && it != TypeName.BOOLEAN) {
                    propParam = ClassName.get(ClassNames.PROPERTY_PACKAGE,
                            it.toString().capitalizeFirstLetter() + "Property")
                } else {
                    propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, it.box())
                }
            }
            typeBuilder.addField(FieldSpec.builder(propParam, it.columnName,
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new \$T(\$T.class, \$S)", propParam, tableClass, it.columnName)
                    .addJavadoc("Foreign Key" + if (isPrimaryKey) " / Primary Key" else "").build())
        }
    }

    override fun addPropertyCase(methodBuilder: MethodSpec.Builder) {
        checkNeedsReferences()
        for (reference in _foreignKeyReferenceDefinitionList) {
            methodBuilder.beginControlFlow("case \$S: ", QueryBuilder.quoteIfNeeded(reference.columnName))
            methodBuilder.addStatement("return \$L", reference.columnName)
            methodBuilder.endControlFlow()
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
                builder.add("\$L", QueryBuilder.quote(referenceDefinition.columnName))
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

    override fun getLoadFromCursorMethod(endNonPrimitiveIf: Boolean, index: AtomicInteger): CodeBlock {
        if (nonModelColumn) {
            return super.getLoadFromCursorMethod(endNonPrimitiveIf, index)
        } else {
            checkNeedsReferences()

            val code = CodeBlock.builder()
            referencedTableClassName?.let {
                val foreignKeyCombiner = ForeignKeyLoadFromCursorCombiner(columnAccessor,
                        it, baseTableDefinition.outputClassName, isStubbedRelationship)
                _foreignKeyReferenceDefinitionList.forEach {
                    foreignKeyCombiner.fieldAccesses += it.partialAccessor
                }
                foreignKeyCombiner.addCode(code, index)
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
        } else {
            if (needsReferences) {
                val primaryColumns = referencedTableDefinition.primaryColumnDefinitions
                primaryColumns.forEach {
                    val foreignKeyReferenceDefinition = ForeignKeyReferenceDefinition(manager,
                            elementName, it, this, primaryColumns.size)
                    _foreignKeyReferenceDefinitionList.add(foreignKeyReferenceDefinition)
                }
                if (nonModelColumn) {
                    columnName = _foreignKeyReferenceDefinitionList[0].columnName
                }
                needsReferences = false
            }

            if (nonModelColumn && _foreignKeyReferenceDefinitionList.size == 1) {
                columnName = _foreignKeyReferenceDefinitionList[0].columnName
            }
        }
    }
}
