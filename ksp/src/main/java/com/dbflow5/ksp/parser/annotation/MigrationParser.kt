package com.dbflow5.ksp.parser.annotation

import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.typeName
import com.dbflow5.codegen.shared.parser.validation.ValidationException
import com.dbflow5.codegen.shared.properties.MigrationProperties

class MigrationParser : AnnotationParser<MigrationProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): MigrationProperties {
        return MigrationProperties(
            version = arg("version"),
            database = typeName("database"),
            priority = arg("priority"),
        )
    }
}
