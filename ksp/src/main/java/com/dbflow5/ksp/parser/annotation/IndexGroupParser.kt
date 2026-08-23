package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.INDEX_GENERIC
import com.dbflow5.codegen.shared.properties.IndexGroupProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.expectedArg

class IndexGroupParser : AnnotationParser<IndexGroupProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): IndexGroupProperties {
        return IndexGroupProperties(
            number = arg("number") ?: INDEX_GENERIC,
            name = expectedArg("name"),
            unique = arg("unique") ?: false,
        )
    }
}