package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.ViewProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class ViewPropertyParser : AnnotationParser<ViewProperties> {

    @Throws(ValidationException::class)
    override fun ArgMap.parse(): ViewProperties {
        return ViewProperties(
            name = arg("name"),
            database = typeName("database"),
            allFields = arg("allFields"),
            orderedCursorLookup = arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = arg("assignDefaultValuesFromCursor"),
            createWithDatabase = arg("createWithDatabase"),
        )
    }
}
