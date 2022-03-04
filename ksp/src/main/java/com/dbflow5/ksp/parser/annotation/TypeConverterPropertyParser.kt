package com.dbflow5.ksp.parser.annotation

import com.dbflow5.codegen.shared.properties.TypeConverterProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toTypeName

class TypeConverterPropertyParser : AnnotationParser<TypeConverterProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): TypeConverterProperties {
        return TypeConverterProperties(
            allowedSubtypeTypeNames = arg<List<KSType>>("allowedSubtypes")
                ?.map { it.toTypeName() } ?: listOf()
        )
    }
}
