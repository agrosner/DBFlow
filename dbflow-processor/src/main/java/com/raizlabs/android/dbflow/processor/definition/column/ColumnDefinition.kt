package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorUtils
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.capitalizeFirstLetter
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

/**
 * Author: andrewgrosner
 * Description:
 */
open class ColumnDefinition @JvmOverloads
constructor(processorManager: ProcessorManager, element: Element,
            var baseTableDefinition: BaseTableDefinition, isPackagePrivate: Boolean,
            var column: Column? = element.getAnnotation<Column>(Column::class.java),
            primaryKey: PrimaryKey? = element.getAnnotation<PrimaryKey>(PrimaryKey::class.java))
: BaseDefinition(element, processorManager) {

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
    var hasCustomConverter: Boolean = false

    init {
        column?.let {
            this.columnName = if (it.name == "")
                element.simpleName.toString()
            else
                it.name
            length = it.length
            collate = it.collate
            defaultValue = it.defaultValue
        }
        if (column == null) {
            this.columnName = element.simpleName.toString()
        }

        if (isPackagePrivate) {
            columnAccess = PackagePrivateAccess.from(processorManager, element,
                    baseTableDefinition.databaseDefinition?.classSeparator)

            // register to ensure we only generate methods that are referenced by these columns.
            PackagePrivateAccess.putElement((columnAccess as PackagePrivateAccess).helperClassName, columnName)
        } else {
            val isPrivate = element.modifiers.contains(Modifier.PRIVATE)
            if (isPrivate) {
                val useIs = elementTypeName?.box() == TypeName.BOOLEAN.box()
                        && baseTableDefinition is TableDefinition && (baseTableDefinition as TableDefinition).useIsForPrivateBooleans
                columnAccess = PrivateColumnAccess(column, useIs)
            } else {
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
            typeConverterClassName = ProcessorUtils.fromTypeMirror(typeMirror)
        }

        hasCustomConverter = false
        if (typeConverterClassName != null && typeMirror != null &&
                typeConverterClassName != ClassNames.TYPE_CONVERTER) {
            val typeConverterDefinition = TypeConverterDefinition(typeConverterClassName, typeMirror, manager)
            if (typeConverterDefinition.modelTypeName != elementTypeName) {
                manager.logError("The specified custom TypeConverter's Model Value %1s from %1s must match the type of the column %1s. ",
                        typeConverterDefinition.modelTypeName, typeConverterClassName, elementTypeName)
            } else {
                hasCustomConverter = true
                val fieldName = baseTableDefinition.addColumnForCustomTypeConverter(this, typeConverterClassName)
                hasTypeConverter = true
                columnAccess = TypeConverterAccess(manager, this, typeConverterDefinition, fieldName)
            }
        }

        if (!hasCustomConverter) {
            val typeElement = ProcessorUtils.getTypeElement(element)
            if (typeElement != null && typeElement.kind == ElementKind.ENUM) {
                columnAccess = EnumColumnAccess(this)
            } else if (elementTypeName == ClassName.get(Blob::class.java)) {
                columnAccess = BlobColumnAccess(this)
            } else {
                if (elementTypeName is ParameterizedTypeName) {
                    // do nothing, for now.
                } else if (elementTypeName is ArrayTypeName) {
                    processorManager.messager.printMessage(Diagnostic.Kind.ERROR,
                            "Columns cannot be of array type.")
                } else {
                    if (elementTypeName == TypeName.BOOLEAN.box()) {
                        isBoolean = true
                        columnAccess = BooleanColumnAccess(manager, this)
                    } else if (elementTypeName == TypeName.BOOLEAN) {
                        // lower case boolean, we don't box up and down, we just check true or false.
                        columnAccess = BooleanTypeColumnAccess(this)
                    } else {
                        // Any annotated members, otherwise we will use the scanner to find other ones
                        val typeConverterDefinition = elementTypeName?.let { processorManager.getTypeConverterDefinition(it) }
                        if (typeConverterDefinition != null ||
                                !SQLiteHelper.containsType(elementTypeName)) {
                            hasTypeConverter = true
                            if (typeConverterDefinition != null) {
                                val fieldName = baseTableDefinition.addColumnForTypeConverter(this,
                                        typeConverterDefinition.className)
                                columnAccess = TypeConverterAccess(manager, this,
                                        typeConverterDefinition, fieldName)
                            } else {
                                columnAccess = TypeConverterAccess(manager, this)
                            }
                        }
                    }
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
            if (elementTypeName.isPrimitive && elementTypeName != TypeName.BOOLEAN) {
                propParam = ClassName.get(ClassNames.PROPERTY_PACKAGE, elementTypeName.toString().capitalizeFirstLetter() + "Property")
            } else {
                propParam = ParameterizedTypeName.get(ClassNames.PROPERTY, elementTypeName.box())
            }

            val fieldBuilder = FieldSpec.builder(propParam,
                    columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("new \$T(\$T.class, \$S)", propParam, tableClass, columnName)
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

    open val insertStatementColumnName: CodeBlock
        get() = CodeBlock.builder().add("\$L", QueryBuilder.quote(columnName)).build()

    open val insertStatementValuesString: CodeBlock
        get() = CodeBlock.builder().add("?").build()

    open fun getContentValuesStatement(isModelContainerAdapter: Boolean): CodeBlock {
        return DefinitionUtils.getContentValuesStatement(elementName, elementName,
                columnName, elementTypeName, columnAccess,
                ModelUtils.variable, defaultValue).build()
    }

    open fun getSQLiteStatementMethod(index: AtomicInteger): CodeBlock {
        return DefinitionUtils.getSQLiteStatementMethod(index, elementName, elementName,
                elementTypeName, columnAccess,
                ModelUtils.variable, isPrimaryKeyAutoIncrement || isRowId, defaultValue).build()
    }

    open fun getLoadFromCursorMethod(endNonPrimitiveIf: Boolean, index: AtomicInteger): CodeBlock {
        return DefinitionUtils.getLoadFromCursorMethod(index.toInt(), elementName,
                elementTypeName, columnName, true, columnAccess,
                baseTableDefinition.orderedCursorLookUp, baseTableDefinition.assignDefaultValuesFromCursor,
                elementName).build()
    }

    /**
     * only used if [.isPrimaryKeyAutoIncrement] is true.

     * @return The statement to use.
     */
    val updateAutoIncrementMethod: CodeBlock
        get() = DefinitionUtils.getUpdateAutoIncrementMethod(elementName, elementName, elementTypeName,
                columnAccess).build()

    fun setColumnAccessString(formattedAccess: CodeBlock): CodeBlock {
        return columnAccess.setColumnAccessString(elementTypeName, elementName, elementName,
                ModelUtils.variable, formattedAccess)
    }

    fun getColumnAccessString(isSqliteStatment: Boolean): CodeBlock {
        return columnAccess.getColumnAccessString(elementTypeName, elementName,
                elementName, ModelUtils.variable, isSqliteStatment)
    }

    open fun appendPropertyComparisonAccessStatement(codeBuilder: CodeBlock.Builder) {
        codeBuilder.add("\nclause.and(\$L.eq(", columnName)
        if (columnAccess is TypeConverterAccess) {
            val converterAccess = columnAccess as TypeConverterAccess
            val converterDefinition = converterAccess.typeConverterDefinition
            converterDefinition?.let {
                codeBuilder.add(converterAccess.existingColumnAccess
                        .getColumnAccessString(converterDefinition.dbTypeName,
                                elementName, elementName, ModelUtils.variable, false))
            }
        } else {
            var columnAccessBlock = getColumnAccessString(false)
            val columnAccessString = columnAccessBlock.toString()
            var subAccessIndex = -1
            if (columnAccess is BlobColumnAccess) {
                subAccessIndex = columnAccessString.lastIndexOf(".getBlob()")
            } else if (columnAccess is EnumColumnAccess) {
                subAccessIndex = columnAccessString.lastIndexOf(".name()")
            } else if (columnAccess is BooleanTypeColumnAccess) {
                subAccessIndex = columnAccessString.lastIndexOf(" ? 1 : 0")
            }
            if (subAccessIndex > 0) {
                columnAccessBlock = CodeBlock.of(columnAccessString.substring(0, subAccessIndex))
            }
            codeBuilder.add(columnAccessBlock)
        }
        codeBuilder.add("));")
    }

    fun getReferenceColumnName(reference: ForeignKeyReference): String {
        return (columnName + "_" + reference.columnName).toUpperCase()
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

    open val primaryKeyName: String
        get() = QueryBuilder.quote(columnName)
}
