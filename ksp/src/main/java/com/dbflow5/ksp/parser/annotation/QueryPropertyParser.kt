package com.dbflow5.ksp.parser.annotation

import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.typeName
import com.dbflow5.codegen.parser.validation.ValidationException
import com.dbflow5.codegen.model.properties.QueryProperties

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
