package com.dbflow5.processor.definition

import com.dbflow5.annotation.TypeConverter
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror

/**
 * Description: Holds data about type converters in order to write them.
 */
class TypeConverterDefinition(
    typeConverter: TypeConverter?,
    val className: ClassName,
    typeMirror: TypeMirror, manager: ProcessorManager,
    val isDefaultConverter: Boolean) {

    val modelTypeName: TypeName?
    val dbTypeName: TypeName?
    val allowedSubTypes: List<TypeName>

    init {
        val allowedSubTypes: MutableList<TypeName> = mutableListOf()
        typeConverter?.let { converter ->
            try {
                converter.allowedSubtypes
            } catch (e: MirroredTypesException) {
                val types = e.typeMirrors
                types.forEach { allowedSubTypes.add(TypeName.get(it)) }
            }
        }
        this.allowedSubTypes = allowedSubTypes

        val types = manager.typeUtils

        var typeConverterSuper: DeclaredType? = null
        val typeConverterType = manager.typeUtils.getDeclaredType(manager.elements
            .getTypeElement(ClassNames.TYPE_CONVERTER.toString()))

        for (superType in types.directSupertypes(typeMirror)) {
            val erasure = types.erasure(superType)
            if (types.isAssignable(erasure, typeConverterType) || erasure.toString() == typeConverterType.toString()) {
                typeConverterSuper = superType as DeclaredType
            }
        }

        if (typeConverterSuper != null) {
            val typeArgs = typeConverterSuper.typeArguments
            dbTypeName = ClassName.get(typeArgs[0])
            modelTypeName = ClassName.get(typeArgs[1])
        } else {
            dbTypeName = null
            modelTypeName = null
        }
    }

}
