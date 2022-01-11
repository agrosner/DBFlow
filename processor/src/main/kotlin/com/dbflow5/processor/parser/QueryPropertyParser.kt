package com.dbflow5.processor.parser

import com.dbflow5.annotation.Query
import com.dbflow5.codegen.model.properties.QueryProperties
import com.dbflow5.codegen.parser.Parser
import com.squareup.kotlinpoet.asTypeName

class QueryPropertyParser : Parser<Query, QueryProperties> {
    override fun parse(input: Query): QueryProperties {
        return QueryProperties(
            database = input.database.asTypeName(),
            allFields = input.allFields,
            orderedCursorLookup = input.orderedCursorLookUp,
            assignDefaultValuesFromCursor = input.assignDefaultValuesFromCursor,
        )
    }
}
