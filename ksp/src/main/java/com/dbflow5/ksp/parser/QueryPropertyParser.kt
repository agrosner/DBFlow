package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.QueryProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class QueryPropertyParser : AnnotationParser<QueryProperties> {

    @Throws(ValidationException::class)
    override fun ArgMap.parse(): QueryProperties {
        return QueryProperties(
            database = typeName("database"),
            allFields = arg("allFields"),
            orderedCursorLookup = arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = arg("assignDefaultValuesFromCursor"),
        )
    }
}
