package com.dbflow5.processor.parser

import com.dbflow5.annotation.Migration
import com.dbflow5.codegen.shared.properties.MigrationProperties
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.squareup.kotlinpoet.javapoet.toKTypeName

class MigrationParser : Parser<Migration, MigrationProperties> {
    override fun parse(input: Migration): MigrationProperties {
        return MigrationProperties(
            version = input.version,
            database = input.extractTypeNameFromAnnotation { it.database }
                .toKTypeName(),
            priority = input.priority,
        )
    }
}
