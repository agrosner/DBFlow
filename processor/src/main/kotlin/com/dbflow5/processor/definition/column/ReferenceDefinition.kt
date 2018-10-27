package com.dbflow5.processor.definition.column

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.data.Blob
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.TypeConverterDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.quote
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Description:
 */
class ReferenceDefinition(private val manager: ProcessorManager,
                          foreignKeyFieldName: String,
                          foreignKeyElementName: String, referencedColumn: ColumnDefinition,
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

    var hasCustomConverter: Boolean = false

    val notNull: Boolean
        get() = onNullConflict != ConflictAction.NONE

    private val isReferencedFieldPrivate = referencedColumn.columnAccessor is PrivateScopeColumnAccessor
    private val isReferencedFieldPackagePrivate: Boolean

    var hasTypeConverter: Boolean = false

    internal val creationStatement: CodeBlock
        get() = DefinitionUtils.getCreationStatement(columnClassName, wrapperTypeName, columnName).build()

    internal val primaryKeyName: String
        get() = columnName.quote()


    private val columnAccessor: ColumnAccessor
    var wrapperAccessor: ColumnAccessor? = null
    var wrapperTypeName: TypeName? = null
    var subWrapperAccessor: ColumnAccessor? = null

    val partialAccessor: PartialLoadFromCursorAccessCombiner
    val primaryReferenceField: ForeignKeyAccessField
    val contentValuesField: ForeignKeyAccessField
    val sqliteStatementField: ForeignKeyAccessField

    private fun handleSpecifiedTypeConverter(typeConverterClassName: ClassName?, typeMirror: TypeMirror?) {
        if (typeConverterClassName != null && typeMirror != null &&
                typeConverterClassName != com.dbflow5.processor.ClassNames.TYPE_CONVERTER) {
            evaluateTypeConverter(TypeConverterDefinition(typeConverterClassName, typeMirror, manager), true)
        }
    }

    private fun evaluateTypeConverter(typeConverterDefinition: TypeConverterDefinition?,
                                      isCustom: Boolean) {
        // Any annotated members, otherwise we will use the scanner to find other ones
        typeConverterDefinition?.let {

            if (it.modelTypeName != columnClassName) {
                manager.logError("The specified custom TypeConverter's Model Value %1s from %1s must match the type of the column %1s. ",
                        it.modelTypeName, it.className, columnClassName)
            } else {
                hasTypeConverter = true
                hasCustomConverter = isCustom

                val fieldName = if (hasCustomConverter) {
                    referenceColumnDefinition.baseTableDefinition
                            .addColumnForCustomTypeConverter(referenceColumnDefinition, it.className)
                } else {
                    referenceColumnDefinition.baseTableDefinition
                            .addColumnForTypeConverter(referenceColumnDefinition, it.className)
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
            isReferencedFieldPrivate -> PrivateScopeColumnAccessor(foreignKeyFieldName, getterSetter, false)
            isReferencedFieldPackagePrivate -> {
                val accessor = PackagePrivateScopeColumnAccessor(foreignKeyFieldName, referencedColumn.packageName,
                        referenceColumnDefinition.baseTableDefinition.databaseDefinition?.classSeparator,
                        tableClassName)
                PackagePrivateScopeColumnAccessor.putElement(accessor.helperClassName, foreignKeyFieldName)

                accessor
            }
            else -> VisibleScopeColumnAccessor(foreignKeyFieldName)
        }

        hasCustomConverter = false

        handleSpecifiedTypeConverter(typeConverterClassName, typeConverterTypeMirror)
        if (!hasCustomConverter) {
            val typeConverterDefinition = columnClassName?.let { manager.getTypeConverterDefinition(it) }
            evaluateTypeConverter(typeConverterDefinition, false)
        }

        val combiner = Combiner(columnAccessor, columnClassName!!, wrapperAccessor,
                wrapperTypeName, subWrapperAccessor, referenceColumnDefinition.elementName)

        partialAccessor = PartialLoadFromCursorAccessCombiner(columnName, foreignColumnName,
                columnClassName, referenceColumnDefinition.baseTableDefinition.orderedCursorLookUp,
                columnAccessor, wrapperAccessor, wrapperTypeName)

        val defaultValue = referenceColumnDefinition.getDefaultValueBlock(this.defaultValue, columnClassName)
        primaryReferenceField = ForeignKeyAccessField(columnName,
                PrimaryReferenceAccessCombiner(combiner), defaultValue)

        contentValuesField = ForeignKeyAccessField(columnName,
                ContentValuesCombiner(combiner), defaultValue)

        sqliteStatementField = ForeignKeyAccessField("",
                SqliteStatementAccessCombiner(combiner), defaultValue)
    }


}
