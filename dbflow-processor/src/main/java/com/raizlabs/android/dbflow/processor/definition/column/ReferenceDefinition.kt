package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition
import com.raizlabs.android.dbflow.processor.utils.ElementUtility
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class ReferenceDefinition(private val manager: ProcessorManager,
                          foreignKeyFieldName: String,
                          foreignKeyElementName: String, referencedColumn: ColumnDefinition,
                          private val referenceColumnDefinition: ReferenceColumnDefinition,
                          referenceCount: Int, localColumnName: String = "",
                          var onNullConflict: ConflictAction = ConflictAction.NONE) {

    val columnName: String
    val foreignColumnName: String
    val columnClassName: TypeName?

    var notNull = false

    var hasTypeConverter: Boolean = false

    internal val creationStatement: CodeBlock
        get() = DefinitionUtils.getCreationStatement(columnClassName, wrapperTypeName, columnName).build()

    internal val primaryKeyName: String
        get() = QueryBuilder.quote(columnName)

    private var isReferencedFieldPrivate: Boolean = false
    private var isReferencedFieldPackagePrivate: Boolean = false

    lateinit var columnAccessor: ColumnAccessor
    var wrapperAccessor: ColumnAccessor? = null
    var wrapperTypeName: TypeName? = null
    var subWrapperAccessor: ColumnAccessor? = null

    lateinit var partialAccessor: PartialLoadFromCursorAccessCombiner
    lateinit var primaryReferenceField: ForeignKeyAccessField
    lateinit var contentValuesField: ForeignKeyAccessField
    lateinit var sqliteStatementField: ForeignKeyAccessField

    private fun createScopes(referenceColumnDefinition: ReferenceColumnDefinition,
                             foreignKeyFieldName: String, getterSetter: GetterSetter,
                             name: String, packageName: String) {
        if (isReferencedFieldPrivate) {
            columnAccessor = PrivateScopeColumnAccessor(foreignKeyFieldName, getterSetter, false)
        } else if (isReferencedFieldPackagePrivate) {
            columnAccessor = PackagePrivateScopeColumnAccessor(foreignKeyFieldName, packageName,
                referenceColumnDefinition.baseTableDefinition.databaseDefinition?.classSeparator,
                name)

            PackagePrivateScopeColumnAccessor.putElement(
                (columnAccessor as PackagePrivateScopeColumnAccessor).helperClassName,
                foreignKeyFieldName)
        } else {
            columnAccessor = VisibleScopeColumnAccessor(foreignKeyFieldName)
        }
    }


    private fun createForeignKeyFields(columnClassName: TypeName?, referenceColumnDefinition: ReferenceColumnDefinition, manager: ProcessorManager) {
        val typeConverterDefinition = columnClassName?.let { manager.getTypeConverterDefinition(it) }
        evaluateTypeConverter(typeConverterDefinition)

        val combiner = Combiner(columnAccessor, columnClassName!!, wrapperAccessor,
            wrapperTypeName, subWrapperAccessor, referenceColumnDefinition.elementName)
        partialAccessor = PartialLoadFromCursorAccessCombiner(columnName, foreignColumnName,
            columnClassName, referenceColumnDefinition.baseTableDefinition.orderedCursorLookUp,
            columnAccessor, wrapperAccessor, wrapperTypeName)

        primaryReferenceField = ForeignKeyAccessField(columnName, PrimaryReferenceAccessCombiner(combiner))

        contentValuesField = ForeignKeyAccessField(columnName, ContentValuesCombiner(combiner))

        sqliteStatementField = ForeignKeyAccessField("start", SqliteStatementAccessCombiner(combiner))

    }

    private fun evaluateTypeConverter(typeConverterDefinition: TypeConverterDefinition?) {
        // Any annotated members, otherwise we will use the scanner to find other ones
        typeConverterDefinition?.let {

            if (it.modelTypeName != columnClassName) {
                manager.logError("The specified custom TypeConverter's Model Value %1s from %1s must match the type of the column %1s. ",
                    it.modelTypeName, it.className, columnClassName)
            } else {
                hasTypeConverter = true

                val fieldName = referenceColumnDefinition.baseTableDefinition
                    .addColumnForTypeConverter(referenceColumnDefinition, it.className)
                wrapperAccessor = TypeConverterScopeColumnAccessor(fieldName)
                wrapperTypeName = it.dbTypeName

                // special case of blob
                if (wrapperTypeName == ClassName.get(Blob::class.java)) {
                    subWrapperAccessor = BlobColumnAccessor()
                }
            }
        }
    }

    init {
        if (!localColumnName.isNullOrEmpty()) {
            this.columnName = localColumnName
        } else if (!referenceColumnDefinition.isPrimaryKey && !referenceColumnDefinition.isPrimaryKeyAutoIncrement
            && !referenceColumnDefinition.isRowId || referenceCount > 0) {
            this.columnName = "${foreignKeyFieldName}_${referencedColumn.columnName}"
        } else {
            this.columnName = foreignKeyFieldName
        }
        foreignColumnName = referencedColumn.columnName
        this.columnClassName = referencedColumn.elementTypeName
        isReferencedFieldPrivate = referencedColumn.columnAccessor is PrivateScopeColumnAccessor
        isReferencedFieldPackagePrivate = referencedColumn.columnAccessor is PackagePrivateScopeColumnAccessor
        val isPackagePrivate = ElementUtility.isPackagePrivate(referencedColumn.element)
        val isPackagePrivateNotInSamePackage = isPackagePrivate &&
            !ElementUtility.isInSamePackage(manager, referencedColumn.element,
                referenceColumnDefinition.element)
        isReferencedFieldPackagePrivate = isReferencedFieldPackagePrivate || isPackagePrivateNotInSamePackage
        val packageName = referencedColumn.packageName
        val name = ClassName.get(referencedColumn.element.enclosingElement as TypeElement).simpleName()
        createScopes(referenceColumnDefinition, foreignKeyElementName, object : GetterSetter {
            override val getterName: String = referencedColumn.column?.getterName ?: ""
            override val setterName: String = referencedColumn.column?.setterName ?: ""
        }, name, packageName)
        createForeignKeyFields(columnClassName, referenceColumnDefinition, manager)

        notNull = onNullConflict != ConflictAction.NONE
    }


}
