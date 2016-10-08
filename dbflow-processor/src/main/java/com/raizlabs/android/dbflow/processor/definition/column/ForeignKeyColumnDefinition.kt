package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.ProcessorUtils
import com.raizlabs.android.dbflow.processor.definition.BindToContentValuesMethod
import com.raizlabs.android.dbflow.processor.definition.BindToStatementMethod
import com.raizlabs.android.dbflow.processor.definition.LoadFromCursorMethod
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
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

        // we need to recheck for this instance
        if (columnAccess is TypeConverterAccess) {
            // is a type converted field
            if (typeElement.modifiers.contains(Modifier.PRIVATE)) {
                val useIs = elementTypeName?.box() == TypeName.BOOLEAN.box() && tableDefinition.useIsForPrivateBooleans
                columnAccess = PrivateColumnAccess(typeElement.getAnnotation(Column::class.java), useIs)
            } else {
                columnAccess = SimpleColumnAccess()
            }
        }

        val references = foreignKey.references
        if (references.size == 0) {
            // no references specified we will delegate references call to post-evaluation
            needsReferences = true
        } else {
            for (reference in references) {
                val referenceDefinition = ForeignKeyReferenceDefinition(manager, elementName, reference, columnAccess, this)
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
            val builder = CodeBlock.builder()
            val statement = columnAccess.getColumnAccessString(elementTypeName, elementName, elementName,
                    ModelUtils.variable, false)
            val finalAccessStatement = getFinalAccessStatement(builder, statement)
            builder.beginControlFlow("if (\$L != null)", finalAccessStatement)

            if (saveForeignKeyModel) {
                builder.addStatement("\$L.save()", finalAccessStatement)
            }

            val elseBuilder = CodeBlock.builder()
            for (referenceDefinition in _foreignKeyReferenceDefinitionList) {
                builder.add(referenceDefinition.contentValuesStatement)
                elseBuilder.addStatement("\$L.putNull(\$S)", BindToContentValuesMethod.PARAM_CONTENT_VALUES, QueryBuilder.quote(referenceDefinition.columnName))
            }

            builder.nextControlFlow("else").add(elseBuilder.build()).endControlFlow()
            builder.build()
        }

    override fun getSQLiteStatementMethod(index: AtomicInteger): CodeBlock {
        if (nonModelColumn) {
            return super.getSQLiteStatementMethod(index)
        } else {
            checkNeedsReferences()
            val builder = CodeBlock.builder()
            val statement = columnAccess.getColumnAccessString(elementTypeName, elementName, elementName,
                    ModelUtils.variable, true)
            val finalAccessStatement = getFinalAccessStatement(builder, statement)
            builder.beginControlFlow("if (\$L != null)", finalAccessStatement)

            if (saveForeignKeyModel) {
                builder.addStatement("\$L.save()", finalAccessStatement)
            }

            val elseBuilder = CodeBlock.builder()
            for (i in _foreignKeyReferenceDefinitionList.indices) {
                if (i > 0) {
                    index.incrementAndGet()
                }
                val referenceDefinition = _foreignKeyReferenceDefinitionList[i]
                builder.add(referenceDefinition.getSQLiteStatementMethod(index))
                elseBuilder.addStatement("\$L.bindNull(\$L)", BindToStatementMethod.PARAM_STATEMENT, "${index.toInt()} + ${BindToStatementMethod.PARAM_START}")
            }

            builder.nextControlFlow("else").add(elseBuilder.build()).endControlFlow()
            return builder.build()
        }
    }

    override fun getLoadFromCursorMethod(endNonPrimitiveIf: Boolean, index: AtomicInteger): CodeBlock {
        if (nonModelColumn) {
            return super.getLoadFromCursorMethod(endNonPrimitiveIf, index)
        } else {
            checkNeedsReferences()
            val builder = CodeBlock.builder()
            val ifNullBuilder = CodeBlock.builder().add("if (")
            val selectBuilder = CodeBlock.builder()

            // used for foreignkey containers only.
            val foreignKeyContainerRefName = "ref" + columnName

            _foreignKeyReferenceDefinitionList.indices.forEach { i ->
                if (i > 0) {
                    index.incrementAndGet()
                }
                val referenceDefinition = _foreignKeyReferenceDefinitionList[i]

                val indexName: String
                if (!baseTableDefinition.orderedCursorLookUp || index.toInt() == -1) {
                    indexName = "index" + referenceDefinition.columnName
                    builder.addStatement("int \$L = \$L.getColumnIndex(\$S)", indexName, LoadFromCursorMethod.PARAM_CURSOR, referenceDefinition.columnName)
                } else {
                    indexName = index.toInt().toString()
                }
                if (i > 0) {
                    ifNullBuilder.add(" && ")
                }

                if (!baseTableDefinition.orderedCursorLookUp || index.toInt() == -1) {
                    ifNullBuilder.add("\$L != -1 && !\$L.isNull(\$L)", indexName, LoadFromCursorMethod.PARAM_CURSOR, indexName)
                } else {
                    ifNullBuilder.add("!\$L.isNull(\$L)", LoadFromCursorMethod.PARAM_CURSOR, indexName)
                }

                val loadFromCursorBlock = CodeBlock.builder().add("\$L.\$L(\$L)", LoadFromCursorMethod.PARAM_CURSOR,
                        DefinitionUtils.getLoadFromCursorMethodString(referenceDefinition.columnClassName,
                                referenceDefinition.columnAccess), indexName).build()
                if (i > 0) {
                    selectBuilder.add("\n")
                }

                if (!isStubbedRelationship) {
                    val generatedTableRef = ClassName.get(
                            referencedTableClassName!!.packageName(),
                            referencedTableClassName!!.simpleName()
                                    + baseTableDefinition.databaseDefinition?.fieldRefSeparator
                                    + TableDefinition.DBFLOW_TABLE_TAG)
                    selectBuilder.add("\n.and(\$L.\$L.eq(\$L))", generatedTableRef,
                            referenceDefinition.foreignColumnName, loadFromCursorBlock)
                } else {
                    selectBuilder.add(referenceDefinition.getForeignKeyContainerMethod(foreignKeyContainerRefName,
                            loadFromCursorBlock))
                }
            }
            ifNullBuilder.add(")")
            builder.beginControlFlow(ifNullBuilder.build().toString())

            val initializer = CodeBlock.builder()


            val selectBlock = selectBuilder.build()

            if (isStubbedRelationship) {
                builder.addStatement("\$T \$L = new \$T()", elementTypeName,
                        foreignKeyContainerRefName, referencedTableClassName)
                builder.add(selectBlock).add("\n")

                initializer.add(foreignKeyContainerRefName)
            } else {
                initializer.add("new \$T().from(\$T.class).where()",
                        ClassNames.SELECT, referencedTableClassName).add(selectBuilder.build()).add(".querySingle()")
            }

            builder.add(columnAccess.setColumnAccessString(elementTypeName, elementName, elementName,
                    ModelUtils.variable, initializer.build()).toBuilder().add(";\n").build())

            if (endNonPrimitiveIf || !baseTableDefinition.assignDefaultValuesFromCursor) {
                builder.endControlFlow()
            }
            return builder.build()
        }
    }

    override fun appendPropertyComparisonAccessStatement(codeBuilder: CodeBlock.Builder) {
        if (nonModelColumn || columnAccess is TypeConverterAccess) {
            super.appendPropertyComparisonAccessStatement(codeBuilder)
        } else {
            val origStatement = getColumnAccessString(false)
            if (isPrimaryKey) {
                var statement: CodeBlock
                val variableName = "container" + elementName
                val typeName = elementTypeName
                codeBuilder.addStatement("\n\$T \$L = (\$T) \$L", typeName, variableName, typeName, origStatement)
                codeBuilder.beginControlFlow("if (\$L != null)", variableName)
                val elseBuilder = CodeBlock.builder()
                for (referenceDefinition in getForeignKeyReferenceDefinitionList()) {
                    if (isModel) {
                        statement = referenceDefinition.primaryReferenceString
                    } else {
                        statement = origStatement
                    }
                    codeBuilder.addStatement("clause.and(\$T.\$L.eq(\$L))", baseTableDefinition.propertyClassName, referenceDefinition.columnName, statement)
                    elseBuilder.addStatement("clause.and(\$T.\$L.eq((\$T) \$L))", baseTableDefinition.propertyClassName, referenceDefinition.columnName, referenceDefinition.columnClassName, DefinitionUtils.getDefaultValueString(referenceDefinition.columnClassName))
                }
                codeBuilder.nextControlFlow("else")
                codeBuilder.add(elseBuilder.build())
                codeBuilder.endControlFlow()
            }
        }
    }

    internal fun getFinalAccessStatement(codeBuilder: CodeBlock.Builder, statement: CodeBlock): CodeBlock {
        var finalAccessStatement = statement
        if (columnAccess is TypeConverterAccess) {
            finalAccessStatement = CodeBlock.of(refName)

            val typeName: TypeName?
            if (columnAccess is TypeConverterAccess) {
                typeName = (columnAccess as TypeConverterAccess).typeConverterDefinition?.dbTypeName
            } else {
                typeName = referencedTableClassName
            }

            typeName?.let {
                codeBuilder.addStatement("\$T \$L = \$L", it, finalAccessStatement, statement)
            }
        }

        return finalAccessStatement
    }

    internal fun getForeignKeyReferenceAccess(statement: CodeBlock): CodeBlock {
        if (columnAccess is TypeConverterAccess) {
            return CodeBlock.of(refName)
        } else {
            return statement
        }
    }

    val refName: String
        get() = "ref" + elementName

    fun getForeignKeyReferenceDefinitionList(): List<ForeignKeyReferenceDefinition> {
        checkNeedsReferences()
        return _foreignKeyReferenceDefinitionList
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
                            elementName, it, columnAccess, this, primaryColumns.size)
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
