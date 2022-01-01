package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.OneToManyProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class OneToManyPropertyParser : AnnotationParser<OneToManyProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): OneToManyProperties {
        return OneToManyProperties(
            childTableType = className("childTable"),
            name = arg("generatedClassName"),
            parentFieldName = arg("parentFieldName"),
            childListFieldName = arg("childListFieldName"),
        )
    }
}
