package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.MigrationProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class MigrationParser : Parser<KSAnnotation, MigrationProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): MigrationProperties {
        val args = input.arguments.mapProperties()
        return MigrationProperties(
            version = args.arg("version"),
            database = args.typeName("database"),
            priority = args.arg("priority"),
        )
    }
}