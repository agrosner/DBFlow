package com.dbflow5.ksp.parser.annotation

import com.dbflow5.codegen.shared.properties.OneToManyProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.className

class OneToManyPropertyParser : AnnotationParser<OneToManyProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): OneToManyProperties {
        return OneToManyProperties(
            childTableType = className("childTable")!!,
            name = arg("generatedClassName") ?: "",
            parentFieldName = arg("parentFieldName") ?: "",
            childListFieldName = arg("childListFieldName") ?: "children",
        )
    }
}
