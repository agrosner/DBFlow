package com.dbflow5.processor.parser

import com.dbflow5.annotation.Query
import com.dbflow5.codegen.shared.properties.QueryProperties
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName

class QueryPropertyParser : Parser<Query, QueryProperties> {
    override fun parse(input: Query): QueryProperties {
        return QueryProperties(
            database = input.extractTypeNameFromAnnotation { it.database }
                .toKTypeName(),
            allFields = input.allFields,
            orderedCursorLookup = input.orderedCursorLookUp,
            assignDefaultValuesFromCursor = input.assignDefaultValuesFromCursor,
        )
    }
}
