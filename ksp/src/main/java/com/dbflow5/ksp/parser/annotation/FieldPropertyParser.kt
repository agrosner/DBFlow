package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.Collate
import com.dbflow5.codegen.shared.properties.FieldProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.converter.TypeConverter
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.className
import com.dbflow5.ksp.parser.enumArg
import com.squareup.kotlinpoet.asClassName

class FieldPropertyParser : AnnotationParser<FieldProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): FieldProperties {
        return FieldProperties(
            name = arg("name") ?: "",
            length = arg("length") ?: -1,
            collate = enumArg("collate", Collate.None, Collate::valueOf),
            defaultValue = arg("defaultValue") ?: "",
            typeConverterClassName = className("typeConverter")
                ?: TypeConverter::class.asClassName(),
        )
    }
}
