package com.dbflow5.model

import com.dbflow5.model.interop.ClassDeclaration
import com.dbflow5.model.interop.OriginatingFileType
import com.dbflow5.model.properties.TypeConverterProperties
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
sealed interface TypeConverterModel : ObjectModel {
    val name: NameModel
    val properties: TypeConverterProperties
    val classType: TypeName
    val dataTypeName: TypeName
    val modelTypeName: TypeName
    val modelClass: ClassDeclaration?

    data class Simple(
        override val name: NameModel,
        override val properties: TypeConverterProperties,
        override val classType: TypeName,
        override val dataTypeName: TypeName,
        override val modelTypeName: TypeName,
        override val modelClass: ClassDeclaration?,
        override val originatingFile: OriginatingFileType?
    ) : TypeConverterModel

    /**
     * Computed variant that specifies we should
     * chain converters until it can resolve to a
     * valid DB type.
     */
    data class Chained(
        override val name: NameModel,
        override val properties: TypeConverterProperties,
        override val classType: TypeName,
        override val modelTypeName: TypeName,
        override val modelClass: ClassDeclaration?,
        /**
         * Nested versions
         */
        val chainedConverters: List<TypeConverterModel>,
        override val originatingFile: OriginatingFileType?,
    ) : TypeConverterModel {
        // based off of last converter
        override val dataTypeName: TypeName
            get() = chainedConverters.last().dataTypeName

        /**
         * Appends a [TypeConverterModel]
         */
        fun append(model: TypeConverterModel): Chained =
            copy(
                chainedConverters = chainedConverters.toMutableList()
                    .apply { add(model) }
            )
    }
}

fun TypeConverterModel.Simple.toChained() =
    TypeConverterModel.Chained(
        name = name,
        properties = properties,
        classType = classType,
        modelTypeName = modelTypeName,
        modelClass = modelClass,
        chainedConverters = listOf(),
        originatingFile = originatingFile,
    )