package com.dbflow5.ksp.parser.annotation

import com.dbflow5.codegen.shared.properties.MigrationProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.expectedArg
import com.dbflow5.ksp.parser.typeName
import com.squareup.kotlinpoet.asTypeName

class MigrationParser : AnnotationParser<MigrationProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): MigrationProperties {
        return MigrationProperties(
            version = expectedArg("version"),
            database = typeName("database") ?: Any::class.asTypeName(),
            priority = arg("priority") ?: -1,
        )
    }
}
