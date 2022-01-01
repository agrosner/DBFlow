package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.IndexProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class IndexParser : AnnotationParser<IndexProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): IndexProperties {
        return IndexProperties(groups = arg("indexGroups"))
    }
}
