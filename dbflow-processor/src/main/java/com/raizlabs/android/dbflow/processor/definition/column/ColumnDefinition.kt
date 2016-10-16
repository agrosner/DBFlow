package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.ProcessorUtils
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition
import com.raizlabs.android.dbflow.processor.utils.capitalizeFirstLetter
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.*
import java.util.*
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
            var column: Column? = element.getAnnotation<Column>(Column::class.java),
            primaryKey: PrimaryKey? = element.getAnnotation<PrimaryKey>(PrimaryKey::class.java))
: BaseDefinition(element, processorManager) {

    private val QUOTE_PATTERN = Pattern.compile("\".*\"")

    var columnName: String = ""

    var hasTypeConverter: Boolean = false
    var isPrimaryKey: Boolean = false
    var isPrimaryKeyAutoIncrement: Boolean = false
        private set
    var isQuickCheckPrimaryKeyAutoIncrement: Boolean = false
    var isRowId: Boolean = false
    var length = -1
    var notNull = false
    var onNullConflict: ConflictAction? = null
    var onUniqueConflict: ConflictAction? = null
    var unique = false

    var uniqueGroups: MutableList<Int> = ArrayList()
    var indexGroups: MutableList<Int> = ArrayList()

    var collate = Collate.NONE
    var defaultValue: String? = null

    var isBoolean = false

    var columnAccess: BaseColumnAccess
    var columnAccessor: ColumnAccessor
    var wrapperAccessor: ColumnAccessor? = null
    var wrapperTypeName: TypeName? = null

    // Wraps for special cases such as for a Blob converter since we cannot use conventional converter
    var subWrapperAccessor: ColumnAccessor? = null

    var hasCustomConverter: Boolean = false

    var typeConverterDefinition: TypeConverterDefinition? = null

    open val insertStatementColumnName: CodeBlock
        get() = CodeBlock.builder().add("\$L", QueryBuilder.quote(columnName)).build()

    open val insertStatementValuesString: CodeBlock? = CodeBlock.builder().add("?").build()


    open val typeConverterElementNames: List<TypeName?>
        get() = arrayListOf(elementTypeName)

    open val primaryKeyName: String? = QueryBuilder.quote(columnName)

    init {
        column?.let {
            this.columnName = if (it.name == "")
                element.simpleName.toString()
            else
                it.name
            length = it.length
            collate = it.collate
            defaultValue = it.defaultValue

            if (elementClassName == ClassName.get(String::class.java)
                    && !QUOTE_PATTERN.matcher(defaultValue).find()) {
                defaultValue = "\"" + defaultValue + "\""
            }
        }
        if (column == null) {
            this.columnName = element.simpleName.toString()
        }

        if (isPackagePrivate) {
            columnAccessor = PackagePrivateScopeColumnAccessor(elementName, packageName,
                    baseTableDefinition.databaseDefinition?.classSeparator,
                    ClassName.get(element.enclosingElement as TypeElement).simpleName())

            PackagePrivateScopeColumnAccessor.putElement(
                    (columnAccessor as PackagePrivateScopeColumnAccessor).helperClassName,
                    columnName)

            columnAccess = PackagePrivateAccess.from(processorManager, element,
                    baseTableDefinition.databaseDefinition?.classSeparator)

            // register to ensure we only generate methods that are referenced by these columns.
            PackagePrivateAccess.putElement((columnAccess as PackagePrivateAccess).helperClassName, columnName)
        } else {
            val isPrivate = element.modifiers.contains(Modifier.PRIVATE)
            if (isPrivate) {
                val isBoolean = elementTypeName?.box() == TypeName.BOOLEAN.box()
                val useIs = isBoolean
                        && baseTableDefinition is TableDefinition && (baseTableDefinition as TableDefinition).useIsForPrivateBooleans
                columnAccess = PrivateColumnAccess(column, useIs)

                columnAccessor = PrivateScopeColumnAccessor(elementName, object : GetterSetter {
                    override val getterName: String = column?.getterName ?: ""
                    override val setterName: String = column?.setterName ?: ""

                }, isBoolean, useIs)

            } else {
                columnAccessor = VisibleScopeColumnAccessor(elementName)
                columnAccess = SimpleColumnAccess()
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

        val uniqueColumn = element.getAnnotation<Unique>(Unique::class.java)
        if (uniqueColumn != null) {
            unique = uniqueColumn.unique
            onUniqueConflict = uniqueColumn.onUniqueConflict
            uniqueColumn.uniqueGroups.forEach { uniqueGroups.add(it) }
        }

        val notNullAnno = element.getAnnotation<NotNull>(NotNull::class.java)
        if (notNullAnno != null) {
            notNull = true
            onNullConflict = notNullAnno.onNullConflict
        }

        val index = element.getAnnotation<Index>(Index::class.java)
        if (index != null) {
            // empty index, we assume generic
            if (index.indexGroups.size == 0) {
                indexGroups.add(IndexGroup.GENERIC)
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
            typeConverterClassName = ProcessorUtils.fromTypeMirror(typeMirror, manager)
        }

        hasCustomConverter = false
        if (typeConverterClassName != null && typeMirror != null &&
                typeConverterClassName != ClassNames.TYPE_CONVERTER) {
            typeConverterDefinition = TypeConverterDefinition(typeConverterClassName, typeMirror, manager)
            evaluateTypeConverter(typeConverterDefinition, true)
        }

        if (!hasCustomConverter) {
            val typeElement = ProcessorUtils.getTypeElement(element)
            if (typeElement != null && typeElement.kind == ElementKind.ENUM) {
                columnAccess = EnumColumnAccess(this)
                wrapperAccessor = EnumColumnAccessor(elementTypeName!!)
                wrapperTypeName = ClassName.get(String::class.java)
            } else if (elementTypeName == ClassName.get(Blob::class.java)) {
                columnAccess = BlobColumnAccess(this)
                wrapperAccessor = BlobColumnAccessor()
                wrapperTypeName = ArrayTypeName.of(TypeName.BYTE)
            } else {
                if (elementTypeName is ParameterizedTypeName) {
                    // do nothing, for now.
                } else if (elementTypeName is ArrayTypeName) {
                    processorManager.messager.printMessage(Diagnostic.Kind.ERROR,
                            "Columns cannot be of array type.")
                } else {
                    if (elementTypeName == TypeName.BOOLEAN) {
                        // lower case boolean, we don't box up and down, we just check true or false.
                        columnAccess = BooleanTypeColumnAccess(this)

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
    }

    private fun evaluateTypeConverter(typeConverterDefinition: TypeConverterDefinition?,
                                      isCustom: Boolean) {
        // Any annotated members, otherwise we will use the scanner to find other ones
        typeConverterDefinition?.let {

            if (it.modelTypeName != elementTypeName) {
                manager.logError("The specified custom TypeConverter's Model Value %1s from %1s must match the type of the column %1s. ",
                        it.modelTypeName, it.className, elementTypeName)
            } else {
                hasTypeConverter = true
                hasCustomConverter = isCustom

                val fieldName = baseTableDefinition.addColumnForTypeConverter(this, it.className)
                if (elementTypeName == TypeName.BOOLEAN.box()) {
                    isBoolean = true
                    columnAccess = BooleanColumnAccess(manager, this)
                } else {
                    columnAccess = TypeConverterAccess(manager, this, it, fieldName)
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
        return QueryBuilder.quoteIfNeeded(columnName)
    }

    open fun addPropertyDefinition(typeBuilder: TypeSpec.Builder, tableClass: TypeName) {
        elementTypeName?.let { elementTypeName ->
            val propParam: TypeName
            if (!wrapperAccessor.isPrimitiveTarget()) {
                propParam = ParameterizedTypeName.get(ClassNames.TYPE_CONVERTED_PROPERTY, elementTypeName.box(),
                        wrapperTypeName)
            } else if (elementTypeName.isPrimitive && elementTypeName != TypeName.BOOLEAN) {
                propParam = ClassName.get(ClassNames.PROPERTY_PACKAGE, elementTypeName.toString().capitalizeFirstLetter() + "Property")
            } else {
                propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, elementTypeName.box())
            }

            val fieldBuilder = FieldSpec.builder(propParam,
                    columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)

            fieldBuilder.initializer("new \$T(\$T.class, \$S)", propParam, tableClass, columnName)
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
            beginControlFlow("case \$S: ", QueryBuilder.quote(columnName))
            addStatement("return \$L", columnName)
            endControlFlow()
        }
    }

    open fun addColumnName(codeBuilder: CodeBlock.Builder) {
        codeBuilder.add(columnName)
    }

    open val contentValuesStatement: CodeBlock
        get() {
            val code = CodeBlock.builder()

            ContentValuesCombiner(columnAccessor, elementTypeName!!, wrapperAccessor,
                    wrapperTypeName, subWrapperAccessor)
                    .addCode(code, columnName, CodeBlock.of(getDefaultValueString()), 0, modelBlock)

            return code.build()
        }

    open fun getSQLiteStatementMethod(index: AtomicInteger): CodeBlock {

        val builder = CodeBlock.builder()
        SqliteStatementAccessCombiner(columnAccessor, elementTypeName!!, wrapperAccessor,
                wrapperTypeName, subWrapperAccessor)
                .addCode(builder, "start", CodeBlock.of(getDefaultValueString()), index.get(), modelBlock)
        return builder.build()
    }

    open fun getLoadFromCursorMethod(endNonPrimitiveIf: Boolean, index: AtomicInteger): CodeBlock {

        val builder = CodeBlock.builder()
        LoadFromCursorAccessCombiner(columnAccessor, elementTypeName!!,
                baseTableDefinition.orderedCursorLookUp, baseTableDefinition.assignDefaultValuesFromCursor,
                wrapperAccessor, wrapperTypeName, subWrapperAccessor)
                .addCode(builder, columnName, CodeBlock.of(getDefaultValueString()), index.get(), modelBlock)
        return builder.build()
    }

    /**
     * only used if [.isPrimaryKeyAutoIncrement] is true.

     * @return The statement to use.
     */
    val updateAutoIncrementMethod: CodeBlock
        get() {
            val code = CodeBlock.builder()
            UpdateAutoIncrementAccessCombiner(columnAccessor, elementTypeName!!,
                    wrapperAccessor, wrapperTypeName, subWrapperAccessor)
                    .addCode(code, columnName, CodeBlock.of(getDefaultValueString()),
                            0, modelBlock)
            return code.build()
        }

    fun getColumnAccessString(index: Int): CodeBlock {
        val codeBlock = CodeBlock.builder()
        CachingIdAccessCombiner(columnAccessor, elementTypeName!!, wrapperAccessor, wrapperTypeName,
                subWrapperAccessor)
                .addCode(codeBlock, columnName, CodeBlock.of(getDefaultValueString()), index, modelBlock)
        return codeBlock.build()
    }

    fun getSimpleAccessString(): CodeBlock {
        val codeBlock = CodeBlock.builder()
        SimpleAccessCombiner(columnAccessor, elementTypeName!!, wrapperAccessor, wrapperTypeName,
                subWrapperAccessor)
                .addCode(codeBlock, columnName, CodeBlock.of(getDefaultValueString()), 0, modelBlock)
        return codeBlock.build()
    }

    open fun appendPropertyComparisonAccessStatement(codeBuilder: CodeBlock.Builder) {
        PrimaryReferenceAccessCombiner(columnAccessor, elementTypeName!!, wrapperAccessor,
                wrapperTypeName, subWrapperAccessor)
                .addCode(codeBuilder, columnName, CodeBlock.of(getDefaultValueString()),
                        0, modelBlock)
    }

    open val creationName: CodeBlock
        get() {
            val codeBlockBuilder = DefinitionUtils.getCreationStatement(elementTypeName, columnAccess, columnName)

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
                codeBlockBuilder.add(" NOT NULL")
            }

            return codeBlockBuilder.build()
        }


    fun getDefaultValueString(): String {
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
                defaultValue = "0"
            } else if (elementTypeName == TypeName.CHAR) {
                defaultValue = "'\\u0000'"
            }
        }
        return defaultValue ?: ""
    }
}
