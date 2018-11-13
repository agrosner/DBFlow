package com.dbflow5.processor.definition.column

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.data.Blob
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.TypeConverterDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.getTypeElement
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.quote
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

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

    private fun evaluateIfWrappingNecessary(element: Element, processorManager: ProcessorManager) {
        if (!hasCustomConverter) {
            val typeElement = getTypeElement(element)
            if (typeElement != null && typeElement.kind == ElementKind.ENUM) {
                wrapperAccessor = EnumColumnAccessor(referencedColumn.elementTypeName!!)
                wrapperTypeName = ClassName.get(String::class.java)
            } else if (referencedColumn.elementTypeName == ClassName.get(Blob::class.java)) {
                wrapperAccessor = BlobColumnAccessor()
                wrapperTypeName = ArrayTypeName.of(TypeName.BYTE)
            } else {
                if (referencedColumn.elementTypeName is ParameterizedTypeName ||
                    referencedColumn.elementTypeName == ArrayTypeName.of(TypeName.BYTE.unbox())) {
                    // do nothing, for now.
                } else if (referencedColumn.elementTypeName is ArrayTypeName) {
                    processorManager.messager.printMessage(Diagnostic.Kind.ERROR,
                        "Columns cannot be of array type. Found ${referencedColumn.elementTypeName}")
                } else {
                    when (referencedColumn.elementTypeName) {
                        TypeName.BOOLEAN -> {
                            wrapperAccessor = BooleanColumnAccessor()
                            wrapperTypeName = TypeName.BOOLEAN
                        }
                        TypeName.CHAR -> {
                            wrapperAccessor = CharColumnAccessor()
                            wrapperTypeName = TypeName.CHAR
                        }
                        TypeName.BYTE -> {
                            wrapperAccessor = ByteColumnAccessor()
                            wrapperTypeName = TypeName.BYTE
                        }
                        else -> evaluateTypeConverter(referencedColumn.elementTypeName?.let {
                            processorManager.getTypeConverterDefinition(it)
                        }, referencedColumn.hasCustomConverter)
                    }
                }
            }
        }
    }

    private fun evaluateTypeConverter(typeConverterDefinition: TypeConverterDefinition?,
                                      isCustom: Boolean) {
        // Any annotated members, otherwise we will use the scanner to find other ones
        typeConverterDefinition?.let { typeConverter ->

            if (typeConverter.modelTypeName != columnClassName) {
                manager.logError("The specified custom TypeConverter's Model Value " +
                    "${typeConverter.modelTypeName} from ${typeConverter.className}" +
                    " must match the type of the column $columnClassName.")
            } else {
                hasTypeConverter = true
                hasCustomConverter = isCustom

                val fieldName = if (hasCustomConverter) {
                    referenceColumnDefinition.entityDefinition
                        .addColumnForCustomTypeConverter(referenceColumnDefinition, typeConverter.className)
                } else {
                    referenceColumnDefinition.entityDefinition
                        .addColumnForTypeConverter(referenceColumnDefinition, typeConverter.className)
                }

                wrapperAccessor = TypeConverterScopeColumnAccessor(fieldName)
                wrapperTypeName = typeConverter.dbTypeName

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
            isReferencedFieldPrivate -> PrivateScopeColumnAccessor(foreignKeyElementName, getterSetter, false)
            isReferencedFieldPackagePrivate -> {
                val accessor = PackagePrivateScopeColumnAccessor(foreignKeyElementName, referencedColumn.packageName,
                    tableClassName)
                PackagePrivateScopeColumnAccessor.putElement(accessor.helperClassName, foreignKeyElementName)

                accessor
            }
            else -> VisibleScopeColumnAccessor(foreignKeyElementName)
        }

        hasCustomConverter = false

        handleSpecifiedTypeConverter(typeConverterClassName, typeConverterTypeMirror)
        if (!hasCustomConverter) {
            val typeConverterDefinition = columnClassName?.let { manager.getTypeConverterDefinition(it) }
            evaluateTypeConverter(typeConverterDefinition, false)
        }
        evaluateIfWrappingNecessary(referencedColumn.element, manager)

        val combiner = Combiner(columnAccessor, columnClassName!!, wrapperAccessor,
            wrapperTypeName, subWrapperAccessor, referenceColumnDefinition.elementName)

        partialAccessor = PartialLoadFromCursorAccessCombiner(
            columnRepresentation = columnName,
            propertyRepresentation = foreignColumnName,
            fieldTypeName = columnClassName,
            orderedCursorLookup = referenceColumnDefinition.entityDefinition.cursorHandlingBehavior.orderedCursorLookup,
            fieldLevelAccessor = columnAccessor,
            subWrapperAccessor = wrapperAccessor,
            subWrapperTypeName = wrapperTypeName
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
