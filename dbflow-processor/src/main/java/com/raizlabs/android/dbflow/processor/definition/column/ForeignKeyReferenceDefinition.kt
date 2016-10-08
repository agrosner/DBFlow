package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

import java.util.concurrent.atomic.AtomicInteger

import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

/**
 * Description:
 */
class ForeignKeyReferenceDefinition {

    private val manager: ProcessorManager
    private val foreignKeyFieldName: String

    val columnName: String
    val foreignColumnName: String
    val columnClassName: TypeName?

    var hasTypeConverter: Boolean = false

    internal val creationStatement: CodeBlock
        get() = DefinitionUtils.getCreationStatement(columnClassName, null, columnName).build()

    internal val primaryKeyName: String
        get() = QueryBuilder.quote(columnName)

    private val foreignKeyColumnVariable: String
        get() = ModelUtils.variable

    private var isReferencedFieldPrivate: Boolean = false
    private var isReferencedFieldPackagePrivate: Boolean = false

    var columnAccess: BaseColumnAccess? = null

    private val tableColumnAccess: BaseColumnAccess
    private val foreignKeyColumnDefinition: ForeignKeyColumnDefinition

    private val simpleColumnAccess: BaseColumnAccess

    constructor(manager: ProcessorManager, foreignKeyFieldName: String,
                referencedColumn: ColumnDefinition,
                tableColumnAccess: BaseColumnAccess,
                foreignKeyColumnDefinition: ForeignKeyColumnDefinition, referenceCount: Int) {
        this.manager = manager
        this.foreignKeyColumnDefinition = foreignKeyColumnDefinition
        this.tableColumnAccess = tableColumnAccess
        this.foreignKeyFieldName = foreignKeyFieldName

        if (!foreignKeyColumnDefinition.isPrimaryKey && !foreignKeyColumnDefinition.isPrimaryKeyAutoIncrement
                && !foreignKeyColumnDefinition.isRowId || referenceCount > 0) {
            this.columnName = foreignKeyFieldName + "_" + referencedColumn.columnName
        } else {
            this.columnName = foreignKeyFieldName
        }
        foreignColumnName = referencedColumn.columnName
        this.columnClassName = referencedColumn.elementTypeName

        if (referencedColumn.columnAccess is WrapperColumnAccess) {
            isReferencedFieldPrivate = (referencedColumn.columnAccess as WrapperColumnAccess).existingColumnAccess is PrivateColumnAccess
            isReferencedFieldPackagePrivate = (referencedColumn.columnAccess as WrapperColumnAccess).existingColumnAccess is PackagePrivateAccess
        } else {
            isReferencedFieldPrivate = referencedColumn.columnAccess is PrivateColumnAccess

            // fix here to ensure we can access it otherwise we generate helper
            val isPackagePrivate = ElementUtility.isPackagePrivate(referencedColumn.element)
            val isPackagePrivateNotInSamePackage = isPackagePrivate &&
                    !ElementUtility.isInSamePackage(manager, referencedColumn.element,
                            foreignKeyColumnDefinition.element)

            isReferencedFieldPackagePrivate = referencedColumn.columnAccess is PackagePrivateAccess || isPackagePrivateNotInSamePackage
        }
        if (isReferencedFieldPrivate) {
            columnAccess = PrivateColumnAccess(referencedColumn.column, false)
        } else if (isReferencedFieldPackagePrivate) {
            columnAccess = PackagePrivateAccess(referencedColumn.packageName,
                    foreignKeyColumnDefinition.baseTableDefinition.databaseDefinition?.classSeparator,
                    ClassName.get(referencedColumn.element.enclosingElement as TypeElement).simpleName())
            PackagePrivateAccess.putElement((columnAccess as PackagePrivateAccess).helperClassName, foreignColumnName)
        } else {
            columnAccess = SimpleColumnAccess()
        }

        val typeConverterDefinition = columnClassName?.let { manager.getTypeConverterDefinition(it) }
        typeConverterDefinition.let {
            if (it != null || !SQLiteHelper.containsType(columnClassName)) {
                hasTypeConverter = true
                if (it != null) {
                    val fieldName = foreignKeyColumnDefinition.baseTableDefinition.addColumnForTypeConverter(foreignKeyColumnDefinition, it.className)
                    columnAccess = TypeConverterAccess(manager, foreignKeyColumnDefinition, it, fieldName)
                } else {
                    columnAccess = TypeConverterAccess(manager, foreignKeyColumnDefinition)
                }
            }
        }

        simpleColumnAccess = SimpleColumnAccess(columnAccess is PackagePrivateAccess
                || columnAccess is TypeConverterAccess)

    }

    constructor(manager: ProcessorManager, foreignKeyFieldName: String,
                foreignKeyReference: ForeignKeyReference, tableColumnAccess: BaseColumnAccess,
                foreignKeyColumnDefinition: ForeignKeyColumnDefinition) {
        this.manager = manager
        this.tableColumnAccess = tableColumnAccess
        this.foreignKeyColumnDefinition = foreignKeyColumnDefinition
        this.foreignKeyFieldName = foreignKeyFieldName

        columnName = foreignKeyReference.columnName
        foreignColumnName = foreignKeyReference.foreignKeyColumnName

        var columnClass: TypeMirror? = null
        try {
            foreignKeyReference.columnType
        } catch (mte: MirroredTypeException) {
            columnClass = mte.typeMirror
        }

        columnClassName = TypeName.get(columnClass!!)
        isReferencedFieldPrivate = foreignKeyReference.referencedFieldIsPrivate
        isReferencedFieldPackagePrivate = foreignKeyReference.referencedFieldIsPackagePrivate
        if (isReferencedFieldPrivate) {
            columnAccess = PrivateColumnAccess(foreignKeyReference)
        } else if (isReferencedFieldPackagePrivate) {
            foreignKeyColumnDefinition.referencedTableClassName?.let {
                columnAccess = PackagePrivateAccess(it.packageName(),
                        foreignKeyColumnDefinition.baseTableDefinition.databaseDefinition?.classSeparator,
                        it.simpleName())
                PackagePrivateAccess.putElement((columnAccess as PackagePrivateAccess).helperClassName, foreignColumnName)
            }
        } else {
            columnAccess = SimpleColumnAccess()
        }

        simpleColumnAccess = SimpleColumnAccess(columnAccess is PackagePrivateAccess)

        val typeConverterDefinition = columnClassName?.let { manager.getTypeConverterDefinition(it) }
        typeConverterDefinition.let {
            if (it != null || !SQLiteHelper.containsType(columnClassName)) {
                hasTypeConverter = true
                if (it != null) {
                    val fieldName = foreignKeyColumnDefinition.baseTableDefinition.addColumnForTypeConverter(foreignKeyColumnDefinition, it.className)
                    columnAccess = TypeConverterAccess(manager, foreignKeyColumnDefinition, it, fieldName)
                } else {
                    columnAccess = TypeConverterAccess(manager, foreignKeyColumnDefinition)
                }
            }
        }
    }

    internal val contentValuesStatement: CodeBlock
        get() {
            var shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, false)
            shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(shortAccess)

            val columnShortAccess = getShortColumnAccess(false, shortAccess)

            val combined: CodeBlock
            if (columnAccess !is PackagePrivateAccess && columnAccess !is TypeConverterAccess) {
                combined = CodeBlock.of("\$L.\$L", shortAccess, columnShortAccess)
            } else {
                combined = columnShortAccess
            }
            return DefinitionUtils.getContentValuesStatement(columnShortAccess.toString(),
                    combined.toString(),
                    columnName, columnClassName, simpleColumnAccess,
                    foreignKeyColumnVariable, null).build()
        }

    val primaryReferenceString: CodeBlock
        get() {
            var shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, false)
            shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(shortAccess)
            val columnShortAccess = getShortColumnAccess(false, shortAccess)
            val combined: CodeBlock
            if (columnAccess !is PackagePrivateAccess && columnAccess !is TypeConverterAccess) {
                combined = CodeBlock.of("\$L.\$L.\$L", ModelUtils.variable,
                        shortAccess, columnShortAccess)
            } else {
                combined = columnShortAccess
            }
            return combined
        }

    internal fun getSQLiteStatementMethod(index: AtomicInteger): CodeBlock {
        var shortAccess = tableColumnAccess.getShortAccessString(foreignKeyColumnDefinition.elementClassName, foreignKeyFieldName, true)
        shortAccess = foreignKeyColumnDefinition.getForeignKeyReferenceAccess(shortAccess)

        val columnShortAccess = getShortColumnAccess(true, shortAccess)
        val combined = shortAccess.toBuilder().add(".").add(columnShortAccess).build()
        return DefinitionUtils.getSQLiteStatementMethod(
                index, columnShortAccess.toString(), combined.toString(),
                columnClassName, simpleColumnAccess,
                foreignKeyColumnVariable, false, null).build()
    }

    internal fun getForeignKeyContainerMethod(referenceFieldName: String, loadFromCursorBlock: CodeBlock): CodeBlock {

        val codeBlock = columnAccess?.setColumnAccessString(columnClassName, foreignColumnName, foreignColumnName,
                referenceFieldName, loadFromCursorBlock)
        val codeBuilder = CodeBlock.builder()
        codeBuilder.addStatement("\$L", codeBlock)
        return codeBuilder.build()
    }

    private fun getShortColumnAccess(isSqliteMethod: Boolean, shortAccess: CodeBlock): CodeBlock {
        columnAccess.let {
            if (it != null) {
                if (it is PackagePrivateAccess || it is TypeConverterAccess) {
                    return it.getColumnAccessString(columnClassName, foreignColumnName,
                            if (it is TypeConverterAccess) foreignColumnName else "",
                            ModelUtils.variable + "." + shortAccess, isSqliteMethod)
                } else {
                    return it.getShortAccessString(columnClassName, foreignColumnName, isSqliteMethod)
                }
            } else {
                return CodeBlock.of("")
            }
        }
    }

}
