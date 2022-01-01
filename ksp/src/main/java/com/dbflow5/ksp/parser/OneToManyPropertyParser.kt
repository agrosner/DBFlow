package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.OneToManyProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class OneToManyPropertyParser : Parser<KSAnnotation, OneToManyProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): OneToManyProperties {
        val args = input.arguments.mapProperties()
        return OneToManyProperties(
            childTableType = args.className("childTable"),
            name = args.arg("generatedClassName"),
            parentFieldName = args.arg("parentFieldName"),
            childListFieldName = args.arg("childListFieldName"),
        )
    }
}