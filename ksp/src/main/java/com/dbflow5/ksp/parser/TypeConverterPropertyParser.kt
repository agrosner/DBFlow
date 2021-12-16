package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.TypeConverterProperties
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Description:
 */
class TypeConverterPropertyParser : Parser<KSAnnotation, TypeConverterProperties> {

    override fun parse(input: KSAnnotation): TypeConverterProperties {
        val args = input.arguments.mapProperties()
        return TypeConverterProperties(
            allowedSubtypeTypeNames = args.arg<List<KSType>>("allowedSubtypes")
                .map { it.toTypeName() }
        )
    }
}