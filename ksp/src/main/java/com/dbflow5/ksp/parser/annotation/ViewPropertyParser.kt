package com.dbflow5.ksp.parser.annotation

import com.dbflow5.codegen.shared.properties.ViewProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.typeName
import com.squareup.kotlinpoet.asTypeName

class ViewPropertyParser : AnnotationParser<ViewProperties> {

    @Throws(ValidationException::class)
    override fun ArgMap.parse(): ViewProperties {
        return ViewProperties(
            name = arg("name") ?: "",
            database = typeName("database") ?: Any::class.asTypeName(),
            allFields = arg("allFields") ?: true,
            orderedCursorLookup = arg("orderedCursorLookUp") ?: false,
            assignDefaultValuesFromCursor = arg("assignDefaultValuesFromCursor") ?: true,
            createWithDatabase = arg("createWithDatabase") ?: true,
        )
    }
}
