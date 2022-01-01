package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.QueryProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class QueryPropertyParser : Parser<KSAnnotation, QueryProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): QueryProperties {
        val args = input.arguments.mapProperties()
        return QueryProperties(
            database = args.typeName("database"),
            allFields = args.arg("allFields"),
            orderedCursorLookup = args.arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = args.arg("assignDefaultValuesFromCursor"),
        )
    }
}