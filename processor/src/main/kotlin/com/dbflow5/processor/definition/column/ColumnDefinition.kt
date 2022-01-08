package com.dbflow5.processor.definition.column

import com.dbflow5.annotation.Collate
import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.INDEX_GENERIC
import com.dbflow5.annotation.Index
import com.dbflow5.annotation.NotNull
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Unique
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.BaseDefinition
import com.dbflow5.processor.definition.EntityDefinition
import com.dbflow5.processor.definition.TableDefinition
import com.dbflow5.processor.definition.behavior.ComplexColumnBehavior
import com.dbflow5.processor.definition.behavior.CursorHandlingBehavior
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.extractTypeMirrorFromAnnotation
import com.dbflow5.processor.utils.fromTypeMirror
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.processor.utils.toClassName
import com.dbflow5.processor.utils.toTypeElement
import com.dbflow5.quote
import com.grosner.kpoet.code
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

open class ColumnDefinition @JvmOverloads
constructor(
    processorManager: ProcessorManager,
    element: Element,
    val entityDefinition: EntityDefinition,
    isPackagePrivate: Boolean,
    val column: Column? = element.annotation(),
    primaryKey: PrimaryKey? = element.annotation(),
    notNullConflict: ConflictAction = ConflictAction.NONE
) : BaseDefinition(element, processorManager) {

    sealed class Type {
        object Normal : Type()
        object Primary : Type()
        data class PrimaryAutoIncrement(val quickCheck: Boolean) : Type()
        object RowId : Type()

        val isPrimaryField
            get() = this is Primary
                || this is PrimaryAutoIncrement
                || this is ColumnDefinition.Type.RowId
    }

    private val QUOTE_PATTERN = Pattern.compile("\".*\"")

    var columnName: String = ""
    var propertyFieldName: String = ""

    var type: Type = Type.Normal

    //var isQuickCheckPrimaryKeyAutoIncrement: Boolean = false
    var length = -1
    var notNull = false
    var isNotNullType = false
    var isNullableType = true
    var onNullConflict: ConflictAction = ConflictAction.FAIL
    var onUniqueConflict: ConflictAction = ConflictAction.FAIL
    var unique = false

    var uniqueGroups: MutableList<Int> = arrayListOf()
    var indexGroups: MutableList<Int> = arrayListOf()

    var collate = Collate.NONE
    var defaultValue: String? = null

    var columnAccessor: ColumnAccessor

    val complexColumnBehavior: ComplexColumnBehavior

    var combiner: Combiner

    open val updateStatementBlock: CodeBlock
        get() = CodeBlock.of("${columnName.quote()}=?")

    open val insertStatementColumnName: CodeBlock
        get() = CodeBlock.of("\$L", columnName.quote())

    open val insertStatementValuesString: CodeBlock?
        get() = if (type is Type.PrimaryAutoIncrement && isNotNullType) {
            CodeBlock.of("nullif(?, 0)")
        } else {
            CodeBlock.of("?")
        }

    open val typeConverterElementNames: List<TypeName?>
        get() = arrayListOf(elementTypeName)

    open val primaryKeyName: String?
        get() = columnName.quote()

    init {
        element.annotation<NotNull>()?.let { notNullAnno ->
            notNull = true
            onNullConflict = notNullAnno.onNullConflict
        }

        if (onNullConflict == ConflictAction.NONE && notNullConflict != ConflictAction.NONE) {
            onNullConflict = notNullConflict
            notNull = true
        }

        if (elementTypeName?.isPrimitive == true) {
            isNullableType = false
            isNotNullType = true
        }

        // if specified, usually from Kotlin targets, we will not set null on the field.
        element.annotation<org.jetbrains.annotations.NotNull>()?.let {
            isNotNullType = true
            isNullableType = false
        }

        // android support annotation
        if (element.annotationMirrors
                .any {
                    val className = it.annotationType.toTypeElement().toClassName()
                    return@any className == ClassNames.NON_NULL || className == ClassNames.NON_NULL_X
                }
        ) {
            isNotNullType = true
            isNullableType = false
        }

        column?.let { column ->
            this.columnName = when {
                column.name == "" -> element.simpleName.toString()
                else -> column.name
            }
            length = column.length
            collate = column.collate
            defaultValue = column.defaultValue

            if (column.defaultValue.isBlank()) {
                defaultValue = null
            }


        }
        if (column == null) {
            this.columnName = element.simpleName.toString()
        }

        val isString = (elementTypeName == ClassName.get(String::class.java))
        if (defaultValue != null
            && isString
            && !QUOTE_PATTERN.matcher(defaultValue).find()
        ) {
            defaultValue = "\"$defaultValue\""
        }

        if (isNotNullType && defaultValue == null
            && isString
        ) {
            defaultValue = "\"\""
        }

        val nameAllocator = NameAllocator()
        propertyFieldName = nameAllocator.newName(this.columnName)

        if (isPackagePrivate) {
            columnAccessor = PackagePrivateScopeColumnAccessor(
                elementName, packageName,
                ClassName.get(element.enclosingElement as TypeElement).simpleName()
            )

            PackagePrivateScopeColumnAccessor.putElement(
                (columnAccessor as PackagePrivateScopeColumnAccessor).helperClassName,
                columnName
            )

        } else {
            val isPrivate = element.modifiers.contains(Modifier.PRIVATE)
            columnAccessor = if (isPrivate) {
                val isBoolean = elementTypeName?.box() == TypeName.BOOLEAN.box()
                val useIs = isBoolean
                    && entityDefinition is TableDefinition && entityDefinition.useIsForPrivateBooleans
                PrivateScopeColumnAccessor(elementName, object : GetterSetter {
                    override val getterName: String = column?.getterName ?: ""
                    override val setterName: String = column?.setterName ?: ""

                }, useIsForPrivateBooleans = useIs)
            } else {
                VisibleScopeColumnAccessor(elementName)
            }
        }

        if (primaryKey != null) {
            type = when {
                primaryKey.rowID -> Type.RowId
                primaryKey.autoincrement -> Type.PrimaryAutoIncrement(quickCheck = primaryKey.quickCheckAutoIncrement)
                else -> Type.Primary
            }
        }

        element.annotation<Unique>()?.let { uniqueColumn ->
            unique = uniqueColumn.unique
            onUniqueConflict = uniqueColumn.onUniqueConflict
            uniqueColumn.uniqueGroups.forEach { uniqueGroups.add(it) }
        }

        element.annotation<Index>()?.let { index ->
            // empty index, we assume generic
            if (index.indexGroups.isEmpty()) {
                indexGroups.add(INDEX_GENERIC)
            } else {
                index.indexGroups.forEach { indexGroups.add(it) }
            }
        }

        val typeMirror = column?.extractTypeMirrorFromAnnotation { it.typeConverter }
        val typeConverterClassName = typeMirror?.let { fromTypeMirror(typeMirror, manager) }

        complexColumnBehavior = ComplexColumnBehavior(
            columnClassName = elementTypeName,
            columnDefinition = this,
            referencedColumn = this,
            referencedColumnHasCustomConverter = false,
            typeConverterClassName = typeConverterClassName,
            typeMirror = typeMirror,
            manager = manager
        )

        combiner = Combiner(
            columnAccessor, elementTypeName!!, complexColumnBehavior.wrapperAccessor,
            complexColumnBehavior.wrapperTypeName,
            complexColumnBehavior.subWrapperAccessor
        )
    }

    override fun toString(): String {
        val tableDef = entityDefinition
        var tableName = tableDef.elementName
        if (tableDef is TableDefinition) {
            tableName = tableDef.associationalBehavior.name
        }
        return "${entityDefinition.databaseDefinition.elementName}.$tableName.${columnName.quote()}"
    }

    open fun addPropertyDefinition(typeBuilder: TypeSpec.Builder, tableClass: TypeName) {
        elementTypeName?.let { elementTypeName ->
            val isNonPrimitiveTypeConverter =
                !complexColumnBehavior.wrapperAccessor.isPrimitiveTarget()
                    && complexColumnBehavior.wrapperAccessor is TypeConverterScopeColumnAccessor
            val propParam: TypeName = if (isNonPrimitiveTypeConverter) {
                ParameterizedTypeName.get(
                    ClassNames.TYPE_CONVERTED_PROPERTY,
                    complexColumnBehavior.wrapperTypeName,
                    elementTypeName.box()
                )
            } else if (!complexColumnBehavior.wrapperAccessor.isPrimitiveTarget()) {
                ParameterizedTypeName.get(
                    ClassNames.WRAPPER_PROPERTY,
                    complexColumnBehavior.wrapperTypeName,
                    elementTypeName.box()
                )
            } else {
                ParameterizedTypeName.get(ClassNames.PROPERTY, elementTypeName.box())
            }

            val fieldBuilder = FieldSpec.builder(
                propParam,
                propertyFieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL
            )

            if (isNonPrimitiveTypeConverter) {
                val codeBlock = CodeBlock.builder()
                codeBlock.add(
                    "new \$T(\$T.getKotlinClass(\$T.class), \$S, true,",
                    propParam,
                    ClassNames.JVM_CLASS_MAPPING,
                    tableClass, columnName
                )
                codeBlock.add(
                    """
                    new ${"$"}T() {
                    @Override
                    public ${"$"}T getTypeConverter(KClass<?> modelClass) {
                        ${"$"}T adapter = (${"$"}T) ${"$"}T.getRetrievalAdapter(modelClass);
                        return adapter.${"$"}L;
                    }
                    })""",
                    ClassNames.TYPE_CONVERTER_GETTER, ClassNames.TYPE_CONVERTER,
                    entityDefinition.outputClassName, entityDefinition.outputClassName,
                    ClassNames.FLOW_MANAGER,
                    (complexColumnBehavior.wrapperAccessor as TypeConverterScopeColumnAccessor).typeConverterFieldName
                )
                fieldBuilder.initializer(codeBlock.build())
            } else {
                fieldBuilder.initializer(
                    "new \$T(\$T.getKotlinClass(\$T.class), \$S)",
                    propParam,
                    ClassNames.JVM_CLASS_MAPPING,
                    tableClass,
                    columnName
                )
            }
            if (type is Type.Primary) {
                fieldBuilder.addJavadoc("Primary Key")
            } else if (type is Type.PrimaryAutoIncrement) {
                fieldBuilder.addJavadoc("Primary Key AutoIncrement")
            }
            typeBuilder.addField(fieldBuilder.build())
        }
    }

    open fun addPropertyCase(methodBuilder: MethodSpec.Builder) {
        methodBuilder.apply {
            beginControlFlow("case \$S: ", columnName.quote())
            addStatement("return \$L", propertyFieldName)
            endControlFlow()
        }
    }

    open fun addColumnName(codeBuilder: CodeBlock.Builder) {
        codeBuilder.add(propertyFieldName)
    }

    open val contentValuesStatement: CodeBlock
        get() {
            val code = CodeBlock.builder()

            ContentValuesCombiner(combiner).apply {
                code.addCode(columnName, getDefaultValueBlock(), 0, modelBlock)
            }

            return code.build()
        }

    open fun appendIndexInitializer(initializer: CodeBlock.Builder, index: AtomicInteger) {
        if (index.get() > 0) {
            initializer.add(", ")
        }
        initializer.add(columnName)
        index.incrementAndGet()
    }

    open fun getSQLiteStatementMethod(index: AtomicInteger, defineProperty: Boolean = true) = code {
        SqliteStatementAccessCombiner(combiner).apply {
            addCode(
                "", getDefaultValueBlock(), index.get(), modelBlock,
                defineProperty
            )
        }
        this
    }

    open fun getLoadFromCursorMethod(
        endNonPrimitiveIf: Boolean, index: AtomicInteger,
        nameAllocator: NameAllocator
    ) = code {
        val (orderedCursorLookup, assignDefaultValuesFromCursor) = entityDefinition.cursorHandlingBehavior
        var assignDefaultValue = assignDefaultValuesFromCursor
        val defaultValueBlock = getDefaultValueBlock()
        if (isNotNullType && CodeBlock.of("null") == defaultValueBlock) {
            assignDefaultValue = false
        }

        LoadFromCursorAccessCombiner(
            combiner, defaultValue != null,
            nameAllocator,
            CursorHandlingBehavior(orderedCursorLookup, assignDefaultValue)
        ).apply {
            addCode(columnName, getDefaultValueBlock(), index.get(), modelBlock)
        }
        this
    }

    /**
     * only used if [.isPrimaryKeyAutoIncrement] is true.

     * @return The statement to use.
     */
    val updateAutoIncrementMethod
        get() = code {
            UpdateAutoIncrementAccessCombiner(combiner).apply {
                addCode(columnName, getDefaultValueBlock(), 0, modelBlock)
            }
            this
        }

    fun getColumnAccessString(index: Int) = code {
        CachingIdAccessCombiner(combiner).apply {
            addCode(columnName, getDefaultValueBlock(), index, modelBlock)
        }
        this
    }

    fun getSimpleAccessString() = code {
        SimpleAccessCombiner(combiner).apply {
            addCode(columnName, getDefaultValueBlock(), 0, modelBlock)
        }
        this
    }

    val quickCheckPrimaryKey: Boolean
        get() = (type as? Type.PrimaryAutoIncrement)?.quickCheck ?: false

    val isAutoRowId: Boolean
        get() = type is Type.RowId || type is Type.PrimaryAutoIncrement

    fun shouldWriteExistence(): Boolean {
        return isAutoRowId || quickCheckPrimaryKey
    }

    open fun appendExistenceMethod(codeBuilder: CodeBlock.Builder) {
        ExistenceAccessCombiner(
            combiner, isAutoRowId,
            quickCheckPrimaryKey,
            entityDefinition.elementClassName!!
        )
            .apply {
                codeBuilder.addCode(columnName, getDefaultValueBlock(), 0, modelBlock)
            }
    }

    open fun appendPropertyComparisonAccessStatement(codeBuilder: CodeBlock.Builder) {
        PrimaryReferenceAccessCombiner(combiner).apply {
            codeBuilder.addCode(propertyFieldName, getDefaultValueBlock(), 0, modelBlock)
        }
    }

    open val creationName: CodeBlock
        get() {
            val codeBlockBuilder = DefinitionUtils.getCreationStatement(
                elementTypeName,
                complexColumnBehavior.wrapperTypeName,
                columnName
            )

            if (type is Type.PrimaryAutoIncrement) {
                codeBlockBuilder.add(" PRIMARY KEY ")

                if (entityDefinition is TableDefinition &&
                    !entityDefinition.primaryKeyConflictActionName.isNullOrEmpty()
                ) {
                    codeBlockBuilder.add(
                        "ON CONFLICT \$L ",
                        entityDefinition.primaryKeyConflictActionName
                    )
                }

                codeBlockBuilder.add("AUTOINCREMENT")
            }

            if (length > -1) {
                codeBlockBuilder.add("(\$L)", length)
            }

            if (collate != Collate.NONE) {
                codeBlockBuilder.add(" COLLATE \$L", collate)
            }

            if (unique) {
                codeBlockBuilder.add(" UNIQUE ON CONFLICT \$L", onUniqueConflict)
            }

            if (notNull || isNotNullType) {
                codeBlockBuilder.add(" NOT NULL ON CONFLICT \$L", onNullConflict)
            }

            return codeBlockBuilder.build()
        }

    fun getDefaultValueBlock(value: String?, elementTypeName: TypeName?): CodeBlock {
        var defaultValue = value
        if (defaultValue.isNullOrEmpty()) {
            defaultValue = "null"
        }
        if (elementTypeName != null && elementTypeName.isPrimitive) {
            if (elementTypeName == TypeName.BOOLEAN) {
                defaultValue = "false"
            } else if (elementTypeName == TypeName.BYTE || elementTypeName == TypeName.INT
                || elementTypeName == TypeName.DOUBLE || elementTypeName == TypeName.FLOAT
                || elementTypeName == TypeName.LONG || elementTypeName == TypeName.SHORT
            ) {
                defaultValue = "($elementTypeName) 0"
            } else if (elementTypeName == TypeName.CHAR) {
                defaultValue = "'\\u0000'"
            }
        }
        return CodeBlock.of(defaultValue)
    }

    fun getDefaultValueBlock() = getDefaultValueBlock(defaultValue, elementTypeName)
}
