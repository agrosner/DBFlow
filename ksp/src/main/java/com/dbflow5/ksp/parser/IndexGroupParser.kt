package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.IndexGroupProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class IndexGroupParser : AnnotationParser<IndexGroupProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): IndexGroupProperties {
        return IndexGroupProperties(
            number = arg("number"),
            name = arg("name"),
            unique = arg("unique"),
        )
    }
}