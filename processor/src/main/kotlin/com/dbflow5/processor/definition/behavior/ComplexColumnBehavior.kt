package com.dbflow5.processor.definition.behavior

import com.dbflow5.data.Blob
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.TypeConverterDefinition
import com.dbflow5.processor.definition.column.BlobColumnAccessor
import com.dbflow5.processor.definition.column.BooleanColumnAccessor
import com.dbflow5.processor.definition.column.ByteColumnAccessor
import com.dbflow5.processor.definition.column.CharColumnAccessor
import com.dbflow5.processor.definition.column.ColumnAccessor
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.EnumColumnAccessor
import com.dbflow5.processor.definition.column.ReferenceDefinition
import com.dbflow5.processor.definition.column.TypeConverterScopeColumnAccessor
import com.dbflow5.processor.utils.getTypeElement
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

/**
 * Description: Consolidates a column's wrapping behavior.
 */
class ComplexColumnBehavior(
    private val columnClassName: TypeName?,

    /**
     * The parent column if within a [ReferenceDefinition], or the column itself if a [ColumnDefinition].
     */
    private val columnDefinition: ColumnDefinition,

    /**
     * The column that it is referencing for type information if its a [ReferenceDefinition].
     * It's itself if its a [ColumnDefinition].
     */
    private val referencedColumn: ColumnDefinition,


    private val referencedColumnHasCustomConverter: Boolean,
    typeConverterClassName: ClassName?,
    typeMirror: TypeMirror?,
    private val manager: ProcessorManager) {

    var hasCustomConverter: Boolean = false
    var hasTypeConverter: Boolean = false

    var wrapperAccessor: ColumnAccessor? = null
    var wrapperTypeName: TypeName? = null

    // Wraps for special cases such as for a Blob converter since we cannot use conventional converter
    var subWrapperAccessor: ColumnAccessor? = null

    init {
        handleSpecifiedTypeConverter(typeConverterClassName, typeMirror)
        evaluateIfWrappingNecessary(referencedColumn.element, manager)
    }

    private fun handleSpecifiedTypeConverter(typeConverterClassName: ClassName?, typeMirror: TypeMirror?) {
        if (typeConverterClassName != null && typeMirror != null &&
            typeConverterClassName != com.dbflow5.processor.ClassNames.TYPE_CONVERTER) {
            evaluateTypeConverter(TypeConverterDefinition(typeConverterClassName, typeMirror, manager), true)
        }
    }

    private fun evaluateIfWrappingNecessary(element: Element,
                                            processorManager: ProcessorManager) {
        val elementTypeName = referencedColumn.elementTypeName
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
                    when (elementTypeName) {
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
                        else -> evaluateTypeConverter(elementTypeName?.let {
                            processorManager.getTypeConverterDefinition(it)
                        }, referencedColumnHasCustomConverter)
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
                    columnDefinition.entityDefinition
                        .addColumnForCustomTypeConverter(columnDefinition, typeConverter.className)
                } else {
                    columnDefinition.entityDefinition
                        .addColumnForTypeConverter(columnDefinition, typeConverter.className)
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
}