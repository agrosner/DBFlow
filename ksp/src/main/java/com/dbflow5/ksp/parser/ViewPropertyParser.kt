package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.ViewProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class ViewPropertyParser : Parser<KSAnnotation, ViewProperties> {

    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): ViewProperties {
        val args = input.arguments.mapProperties()
        return ViewProperties(
            name = args.arg("name"),
            database = args.typeName("database"),
            allFields = args.arg("allFields"),
            orderedCursorLookup = args.arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = args.arg("assignDefaultValuesFromCursor"),
            createWithDatabase = args.arg("createWithDatabase"),
        )
    }
}