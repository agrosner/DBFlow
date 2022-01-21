package com.dbflow5.processor.parser

import com.dbflow5.annotation.ModelView
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.properties.ViewProperties
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.squareup.kotlinpoet.javapoet.toKTypeName

class ViewPropertyParser : Parser<ModelView, ViewProperties> {
    override fun parse(input: ModelView): ViewProperties {
        return ViewProperties(
            name = input.name,
            database = input.extractTypeNameFromAnnotation { it.database }
                .toKTypeName(),
            allFields = input.allFields,
            orderedCursorLookup = input.orderedCursorLookUp,
            assignDefaultValuesFromCursor = input.assignDefaultValuesFromCursor,
            createWithDatabase = input.createWithDatabase,
        )
    }
}
