package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.IndexGroupProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class IndexGroupParser : Parser<KSAnnotation, IndexGroupProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): IndexGroupProperties {
        val args = input.arguments.mapProperties()
        return IndexGroupProperties(
            number = args.arg("number"),
            name = args.arg("name"),
            unique = args.arg("unique"),
        )
    }
}