package com.raizlabs.android.dbflow.processor.definition.column

import com.grosner.kpoet.code
import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.INDEX_GENERIC
import com.raizlabs.android.dbflow.annotation.Index
import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Unique
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition
import com.raizlabs.android.dbflow.processor.utils.annotation
import com.raizlabs.android.dbflow.processor.utils.fromTypeMirror
import com.raizlabs.android.dbflow.processor.utils.getTypeElement
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.processor.utils.toClassName
import com.raizlabs.android.dbflow.processor.utils.toTypeElement
import com.raizlabs.android.dbflow.quote
import com.squareup.javapoet.ArrayTypeName
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
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

open class ColumnDefinition @JvmOverloads
constructor(processorManager: ProcessorManager, element: Element,
            var baseTableDefinition: BaseTableDefinition, isPackagePrivate: Boolean,
            var column: Column? = element.annotation(),
            primaryKey: PrimaryKey? = element.annotation(),
            notNullConflict: ConflictAction = ConflictAction.NONE)
    : BaseDefinition(element, processorManager) {

    private val QUOTE_PATTERN = Pattern.compile("\".*\"")

    var columnName: String = ""
    var propertyFieldName: String = ""

    var hasTypeConverter: Boolean = false
    var isPrimaryKey: Boolean = false
    var isPrimaryKeyAutoIncrement: Boolean = false
        private set
    var isQuickCheckPrimaryKeyAutoIncrement: Boolean = false
    var isRowId: Boolean = false
    var length = -1
    var notNull = false
    var isNotNullType = false
    var isNullableType = true
    var onNullConflict: ConflictAction? = null
    var onUniqueConflict: ConflictAction? = null
    var unique = false

    var uniqueGroups: MutableList<Int> = arrayListOf()
    var indexGroups: MutableList<Int> = arrayListOf()

    var collate = Collate.NONE
    var defaultValue: String? = null

    var columnAccessor: ColumnAccessor
    var wrapperAccessor: ColumnAccessor? = null
    var wrapperTypeName: TypeName? = null

    // Wraps for special cases such as for a Blob converter since we cannot use conventional converter
    var subWrapperAccessor: ColumnAccessor? = null

    var combiner: Combiner

    var hasCustomConverter: Boolean = false

    var typeConverterDefinition: TypeConverterDefinition? = null

    open val updateStatementBlock: CodeBlock
        get() = CodeBlock.of("${columnName.quote()}=?")

    open val insertStatementColumnName: CodeBlock
        get() = CodeBlock.of("\$L", columnName.quote())

    open val insertStatementValuesString: CodeBlock? = CodeBlock.of("?")

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
        element.annotationMirrors
                .find { it.annotationType.toTypeElement().toClassName() == ClassNames.NON_NULL }?.let {
            isNotNullType = true
            isNullableType = false
        }

        column?.let {
            this.columnName = if (it.name == "")
                element.simpleName.toString()
            else
                it.name
            length = it.length
            collate = it.collate
            defaultValue = it.defaultValue

            if (it.defaultValue.isBlank()) {
                defaultValue = null
            }


        }
        if (column == null) {
            this.columnName = element.simpleName.toString()
        }

        val isString = (elementTypeName == ClassName.get(String::class.java))
        if (defaultValue != null
                && isString
                && !QUOTE_PATTERN.matcher(defaultValue).find()) {
            defaultValue = "\"" + defaultValue + "\""
        }

        if (isNotNullType && defaultValue == null
                && isString) {
            defaultValue = "\"\""
        }

        val nameAllocator = NameAllocator()
        propertyFieldName = nameAllocator.newName(this.columnName)

        if (isPackagePrivate) {
            columnAccessor = PackagePrivateScopeColumnAccessor(elementName, packageName,
                    baseTableDefinition.databaseDefinition?.classSeparator,
                    ClassName.get(element.enclosingElement as TypeElement).simpleName())

            PackagePrivateScopeColumnAccessor.putElement(
                    (columnAccessor as PackagePrivateScopeColumnAccessor).helperClassName,
                    columnName)

        } else {
            val isPrivate = element.modifiers.contains(Modifier.PRIVATE)
            if (isPrivate) {
                val isBoolean = elementTypeName?.box() == TypeName.BOOLEAN.box()
                val useIs = isBoolean
                        && baseTableDefinition is TableDefinition && (baseTableDefinition as TableDefinition).useIsForPrivateBooleans
                columnAccessor = PrivateScopeColumnAccessor(elementName, object : GetterSetter {
                    override val getterName: String = column?.getterName ?: ""
                    override val setterName: String = column?.setterName ?: ""

                }, useIsForPrivateBooleans = useIs)

            } else {
                columnAccessor = VisibleScopeColumnAccessor(elementName)
            }
        }

        if (primaryKey != null) {
            if (primaryKey.rowID) {
                isRowId = true
            } else if (primaryKey.autoincrement) {
                isPrimaryKeyAutoIncrement = true
                isQuickCheckPrimaryKeyAutoIncrement = primaryKey.quickCheckAutoIncrement
            } else {
                isPrimaryKey = true
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

        var typeConverterClassName: ClassName? = null
        var typeMirror: TypeMirror? = null
        try {
            column?.typeConverter
        } catch (mte: MirroredTypeException) {
            typeMirror = mte.typeMirror
            typeConverterClassName = fromTypeMirror(typeMirror, manager)
        }

        hasCustomConverter = false
        if (typeConverterClassName != null && typeMirror != null &&
                typeConverterClassName != ClassNames.TYPE_CONVERTER) {
            typeConverterDefinition = TypeConverterDefinition(typeConverterClassName, typeMirror, manager)
            evaluateTypeConverter(typeConverterDefinition, true)
        }

        if (!hasCustomConverter) {
            val typeElement = getTypeElement(element)
            if (typeElement != null && typeElement.kind == ElementKind.ENUM) {
                wrapperAccessor = EnumColumnAccessor(elementTypeName!!)
                wrapperTypeName = ClassName.get(String::class.java)
            } else if (elementTypeName == ClassName.get(Blob::class.java)) {
                wrapperAccessor = BlobColumnAccessor()
                wrapperTypeName = ArrayTypeName.of(TypeName.BYTE)
            } else {
                if (elementTypeName is ParameterizedTypeName ||
                        elementTypeName == ArrayTypeName.of(TypeName.BYTE.unbox())) {
                    // do nothing, for now.
                } else if (elementTypeName is ArrayTypeName) {
                    processorManager.messager.printMessage(Diagnostic.Kind.ERROR,
                            "Columns cannot be of array type. Found $elementTypeName")
                } else {
                    if (elementTypeName == TypeName.BOOLEAN) {
                        wrapperAccessor = BooleanColumnAccessor()
                        wrapperTypeName = TypeName.BOOLEAN
                    } else if (elementTypeName == TypeName.CHAR) {
                        wrapperAccessor = CharColumnAccessor()
                        wrapperTypeName = TypeName.CHAR
                    } else if (elementTypeName == TypeName.BYTE) {
                        wrapperAccessor = ByteColumnAccessor()
                        wrapperTypeName = TypeName.BYTE
                    } else {
                        typeConverterDefinition = elementTypeName?.let { processorManager.getTypeConverterDefinition(it) }
                        evaluateTypeConverter(typeConverterDefinition, false)
                    }
                }
            }
        }

        combiner = Combiner(columnAccessor, elementTypeName!!, wrapperAccessor, wrapperTypeName,
                subWrapperAccessor)
    }

    private fun evaluateTypeConverter(typeConverterDefinition: TypeConverterDefinition?,
                                      isCustom: Boolean) {
        // Any annotated members, otherwise we will use the scanner to find other ones
        typeConverterDefinition?.let {

            if (it.modelTypeName != elementTypeName) {
                manager.logError("The specified custom TypeConverter's Model Value ${it.modelTypeName}" +
                        " from ${it.className} must match the type of the column $elementTypeName. ")
            } else {
                hasTypeConverter = true
                hasCustomConverter = isCustom

                val fieldName = if (hasCustomConverter) {
                    baseTableDefinition.addColumnForCustomTypeConverter(this, it.className)
                } else {
                    baseTableDefinition.addColumnForTypeConverter(this, it.className)
                }
                wrapperAccessor = TypeConverterScopeColumnAccessor(fieldName)
                wrapperTypeName = it.dbTypeName

                // special case of blob
                if (wrapperTypeName == ClassName.get(Blob::class.java)) {
                    subWrapperAccessor = BlobColumnAccessor()
                }
            }
        }
    }

    override fun toString(): String {
        val tableDef = baseTableDefinition
        var tableName = tableDef.elementName
        if (tableDef is TableDefinition) {
            tableName = tableDef.tableName ?: ""
        }
        return "${baseTableDefinition.databaseDefinition?.databaseClassName}.$tableName.${columnName.quote()}"
    }

    open fun addPropertyDefinition(typeBuilder: TypeSpec.Builder, tableClass: TypeName) {
        elementTypeName?.let { elementTypeName ->
            val propParam: TypeName

            val isNonPrimitiveTypeConverter = !wrapperAccessor.isPrimitiveTarget() && wrapperAccessor is TypeConverterScopeColumnAccessor
            if (isNonPrimitiveTypeConverter) {
                propParam = ParameterizedTypeName.get(ClassNames.TYPE_CONVERTED_PROPERTY, wrapperTypeName, elementTypeName.box())
            } else if (!wrapperAccessor.isPrimitiveTarget()) {
                propParam = ParameterizedTypeName.get(ClassNames.WRAPPER_PROPERTY, wrapperTypeName, elementTypeName.box())
            } else {
                propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, elementTypeName.box())
            }

            val fieldBuilder = FieldSpec.builder(propParam,
                    propertyFieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)

            if (isNonPrimitiveTypeConverter) {
                val codeBlock = CodeBlock.builder()
                codeBlock.add("new \$T(\$T.class, \$S, true,", propParam, tableClass, columnName)
                codeBlock.add("\nnew \$T() {" +
                        "\n@Override" +
                        "\npublic \$T getTypeConverter(Class<?> modelClass) {" +
                        "\n  \$T adapter = (\$T) \$T.getInstanceAdapter(modelClass);" +
                        "\nreturn adapter.\$L;" +
                        "\n}" +
                        "\n})", ClassNames.TYPE_CONVERTER_GETTER, ClassNames.TYPE_CONVERTER,
                        baseTableDefinition.outputClassName, baseTableDefinition.outputClassName,
                        ClassNames.FLOW_MANAGER,
                        (wrapperAccessor as TypeConverterScopeColumnAccessor).typeConverterFieldName)
                fieldBuilder.initializer(codeBlock.build())
            } else {
                fieldBuilder.initializer("new \$T(\$T.class, \$S)", propParam, tableClass, columnName)
            }
            if (isPrimaryKey) {
                fieldBuilder.addJavadoc("Primary Key")
            } else if (isPrimaryKeyAutoIncrement) {
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

    open fun getSQLiteStatementMethod(index: AtomicInteger, useStart: Boolean,
                                      defineProperty: Boolean = true) = code {
        SqliteStatementAccessCombiner(combiner).apply {
            addCode(if (useStart) "start" else "", getDefaultValueBlock(), index.get(), modelBlock,
                    defineProperty)
        }
        this
    }

    open fun getLoadFromCursorMethod(endNonPrimitiveIf: Boolean, index: AtomicInteger,
                                     nameAllocator: NameAllocator) = code {

        var assignDefaultValue = baseTableDefinition.assignDefaultValuesFromCursor
        val defaultValueBlock = getDefaultValueBlock()
        if (isNotNullType && CodeBlock.of("null") == defaultValueBlock) {
            assignDefaultValue = false
        }

        LoadFromCursorAccessCombiner(combiner, defaultValue != null,
                nameAllocator, baseTableDefinition.orderedCursorLookUp,
                assignDefaultValue).apply {
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

    open fun appendExistenceMethod(codeBuilder: CodeBlock.Builder) {
        ExistenceAccessCombiner(combiner, isRowId || isPrimaryKeyAutoIncrement,
                isQuickCheckPrimaryKeyAutoIncrement, baseTableDefinition.elementClassName!!)
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
            val codeBlockBuilder = DefinitionUtils.getCreationStatement(elementTypeName, wrapperTypeName, columnName)

            if (isPrimaryKeyAutoIncrement && !isRowId) {
                codeBlockBuilder.add(" PRIMARY KEY ")

                if (baseTableDefinition is TableDefinition &&
                        !(baseTableDefinition as TableDefinition).primaryKeyConflictActionName.isNullOrEmpty()) {
                    codeBlockBuilder.add("ON CONFLICT \$L ",
                            (baseTableDefinition as TableDefinition).primaryKeyConflictActionName)
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

            if (notNull) {
                codeBlockBuilder.add(" NOT NULL ON CONFLICT \$L", onNullConflict)
            }

            return codeBlockBuilder.build()
        }


    fun getDefaultValueBlock(): CodeBlock {
        var defaultValue = defaultValue
        if (defaultValue.isNullOrEmpty()) {
            defaultValue = "null"
        }
        val elementTypeName = this.elementTypeName
        if (elementTypeName != null && elementTypeName.isPrimitive) {
            if (elementTypeName == TypeName.BOOLEAN) {
                defaultValue = "false"
            } else if (elementTypeName == TypeName.BYTE || elementTypeName == TypeName.INT
                    || elementTypeName == TypeName.DOUBLE || elementTypeName == TypeName.FLOAT
                    || elementTypeName == TypeName.LONG || elementTypeName == TypeName.SHORT) {
                defaultValue = "($elementTypeName) 0"
            } else if (elementTypeName == TypeName.CHAR) {
                defaultValue = "'\\u0000'"
            }
        }
        return CodeBlock.of(defaultValue)
    }
}
