package com.dbflow5.processor.parser

import com.dbflow5.annotation.TypeConverter
import com.dbflow5.codegen.model.properties.TypeConverterProperties
import com.dbflow5.codegen.parser.Parser
import com.squareup.kotlinpoet.asTypeName

class TypeConverterPropertyParser : Parser<TypeConverter,
    TypeConverterProperties> {
    override fun parse(input: TypeConverter): TypeConverterProperties {
        return TypeConverterProperties(
            allowedSubtypeTypeNames = input.allowedSubtypes.map { it.asTypeName() },
        )
    }
}
