package com.dbflow5.processor.definition.column

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.behavior.ComplexColumnBehavior
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.quote
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Description:
 */
class ReferenceDefinition(private val manager: ProcessorManager,
                          foreignKeyFieldName: String,
                          foreignKeyElementName: String,
                          private val referencedColumn: ColumnDefinition,
                          private val referenceColumnDefinition: ReferenceColumnDefinition,
                          referenceCount: Int, localColumnName: String = "",
                          var onNullConflict: ConflictAction = ConflictAction.NONE,
                          val defaultValue: String?,
                          typeConverterClassName: ClassName? = null,
                          typeConverterTypeMirror: TypeMirror? = null) {

    val columnName: String = when {
        !localColumnName.isNullOrEmpty() -> localColumnName
        !referenceColumnDefinition.type.isPrimaryField || referenceCount > 0 ->
            "${foreignKeyFieldName}_${referencedColumn.columnName}"
        else -> foreignKeyFieldName
    }
    val foreignColumnName = referencedColumn.columnName
    val columnClassName = referencedColumn.elementTypeName

    val notNull: Boolean
        get() = onNullConflict != ConflictAction.NONE

    private val isReferencedFieldPrivate = referencedColumn.columnAccessor is PrivateScopeColumnAccessor
    private val isReferencedFieldPackagePrivate: Boolean


    internal val creationStatement: CodeBlock
        get() = DefinitionUtils.getCreationStatement(columnClassName, complexColumnBehavior.wrapperTypeName, columnName).build()

    internal val primaryKeyName: String
        get() = columnName.quote()

    val hasTypeConverter
        get() = complexColumnBehavior.hasTypeConverter

    private val columnAccessor: ColumnAccessor
    private val complexColumnBehavior: ComplexColumnBehavior

    val partialAccessor: PartialLoadFromCursorAccessCombiner
    val primaryReferenceField: ForeignKeyAccessField
    val contentValuesField: ForeignKeyAccessField
    val sqliteStatementField: ForeignKeyAccessField

    init {
        val isPackagePrivate = ElementUtility.isPackagePrivate(referencedColumn.element)
        val isPackagePrivateNotInSamePackage = isPackagePrivate &&
            !ElementUtility.isInSamePackage(manager, referencedColumn.element,
                referenceColumnDefinition.element)

        isReferencedFieldPackagePrivate = referencedColumn.columnAccessor is PackagePrivateScopeColumnAccessor
            || isPackagePrivateNotInSamePackage

        val tableClassName = ClassName.get(referencedColumn.element.enclosingElement as TypeElement).simpleName()
        val getterSetter = object : GetterSetter {
            override val getterName: String = referencedColumn.column?.getterName ?: ""
            override val setterName: String = referencedColumn.column?.setterName ?: ""
        }
        columnAccessor = when {
            isReferencedFieldPrivate -> PrivateScopeColumnAccessor(foreignKeyElementName, getterSetter, false)
            isReferencedFieldPackagePrivate -> {
                val accessor = PackagePrivateScopeColumnAccessor(foreignKeyElementName, referencedColumn.packageName,
                    tableClassName)
                PackagePrivateScopeColumnAccessor.putElement(accessor.helperClassName, foreignKeyElementName)

                accessor
            }
            else -> VisibleScopeColumnAccessor(foreignKeyElementName)
        }

        complexColumnBehavior = ComplexColumnBehavior(
            columnClassName = columnClassName,
            columnDefinition = referenceColumnDefinition,
            referencedColumn = referencedColumn,
            referencedColumnHasCustomConverter = referencedColumn.complexColumnBehavior.hasCustomConverter,
            typeConverterClassName = typeConverterClassName,
            typeMirror = typeConverterTypeMirror,
            manager = manager
        )

        val combiner = Combiner(columnAccessor, columnClassName!!, complexColumnBehavior.wrapperAccessor,
            complexColumnBehavior.wrapperTypeName, complexColumnBehavior.subWrapperAccessor, referenceColumnDefinition.elementName)

        partialAccessor = PartialLoadFromCursorAccessCombiner(
            columnRepresentation = columnName,
            propertyRepresentation = foreignColumnName,
            fieldTypeName = columnClassName,
            orderedCursorLookup = referenceColumnDefinition.entityDefinition.cursorHandlingBehavior.orderedCursorLookup,
            fieldLevelAccessor = columnAccessor,
            subWrapperAccessor = complexColumnBehavior.wrapperAccessor,
            subWrapperTypeName = complexColumnBehavior.wrapperTypeName
        )

        val defaultValue = referenceColumnDefinition.getDefaultValueBlock(this.defaultValue, columnClassName)
        primaryReferenceField = ForeignKeyAccessField(columnName,
            PrimaryReferenceAccessCombiner(combiner), defaultValue)

        contentValuesField = ForeignKeyAccessField(columnName,
            ContentValuesCombiner(combiner), defaultValue)

        sqliteStatementField = ForeignKeyAccessField("",
            SqliteStatementAccessCombiner(combiner), defaultValue)
    }

}
