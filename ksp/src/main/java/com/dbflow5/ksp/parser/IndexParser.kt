package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.IndexProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class IndexParser : Parser<KSAnnotation, IndexProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): IndexProperties {
        val args = input.arguments.mapProperties()
        return IndexProperties(
            groups = args.arg("indexGroups")
        )
    }
}