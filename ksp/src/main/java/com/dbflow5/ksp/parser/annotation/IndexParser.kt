package com.dbflow5.ksp.parser.annotation

import com.dbflow5.ksp.model.properties.IndexProperties
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.validation.ValidationException

class IndexParser : AnnotationParser<IndexProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): IndexProperties {
        return IndexProperties(groups = arg("indexGroups"))
    }
}
