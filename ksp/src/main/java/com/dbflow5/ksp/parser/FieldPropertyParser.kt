package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Collate
import com.dbflow5.ksp.model.properties.FieldProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class FieldPropertyParser : AnnotationParser<FieldProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): FieldProperties {
        return FieldProperties(
            name = arg("name"),
            length = arg("length"),
            collate = enumArg("collate", Collate::valueOf),
            defaultValue = arg("defaultValue"),
            typeConverterClassName = className("typeConverter"),
        )
    }
}
