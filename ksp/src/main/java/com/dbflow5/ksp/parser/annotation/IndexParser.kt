package com.dbflow5.ksp.parser.annotation

import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.codegen.shared.parser.validation.ValidationException
import com.dbflow5.codegen.shared.properties.IndexProperties

class IndexParser : AnnotationParser<IndexProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): IndexProperties {
        return IndexProperties(groups = arg("indexGroups"))
    }
}
