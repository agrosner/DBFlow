package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.MigrationProperties
import com.dbflow5.ksp.parser.validation.ValidationException

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
