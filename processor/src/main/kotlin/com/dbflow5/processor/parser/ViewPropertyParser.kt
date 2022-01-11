package com.dbflow5.processor.parser

import com.dbflow5.annotation.ModelView
import com.dbflow5.codegen.model.properties.ViewProperties
import com.dbflow5.codegen.parser.Parser
import com.squareup.kotlinpoet.asTypeName

class ViewPropertyParser : Parser<ModelView, ViewProperties> {
    override fun parse(input: ModelView): ViewProperties {
        return ViewProperties(
            name = input.name,
            database = input.database.asTypeName(),
            allFields = input.allFields,
            orderedCursorLookup = input.orderedCursorLookUp,
            assignDefaultValuesFromCursor = input.assignDefaultValuesFromCursor,
            createWithDatabase = input.createWithDatabase,
        )
    }
}
