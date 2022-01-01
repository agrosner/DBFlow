package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Collate
import com.dbflow5.ksp.model.properties.FieldProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class FieldPropertyParser : Parser<KSAnnotation, FieldProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): FieldProperties {
        val args = input.arguments.mapProperties()
        return FieldProperties(
            name = args.arg("name"),
            length = args.arg("length"),
            collate = args.enumArg("collate", Collate::valueOf),
            defaultValue = args.arg("defaultValue"),
            typeConverterClassName = args.className("typeConverter"),
        )
    }
}