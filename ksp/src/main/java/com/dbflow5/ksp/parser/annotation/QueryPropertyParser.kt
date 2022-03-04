package com.dbflow5.ksp.parser.annotation

import com.dbflow5.codegen.shared.properties.QueryProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.typeName
import com.squareup.kotlinpoet.asTypeName

class QueryPropertyParser : AnnotationParser<QueryProperties> {

    @Throws(ValidationException::class)
    override fun ArgMap.parse(): QueryProperties {
        return QueryProperties(
            database = typeName("database") ?: Any::class.asTypeName(),
            allFields = arg("allFields") ?: true,
            orderedCursorLookup = arg("orderedCursorLookUp") ?: false,
            assignDefaultValuesFromCursor = arg("assignDefaultValuesFromCursor") ?: true,
        )
    }
}
