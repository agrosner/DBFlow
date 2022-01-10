package com.dbflow5.ksp.parser.annotation

import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.codegen.parser.validation.ValidationException
import com.dbflow5.codegen.model.properties.IndexGroupProperties

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