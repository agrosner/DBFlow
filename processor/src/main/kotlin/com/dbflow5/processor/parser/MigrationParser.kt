package com.dbflow5.processor.parser

import com.dbflow5.annotation.Migration
import com.dbflow5.codegen.model.properties.MigrationProperties
import com.dbflow5.codegen.parser.Parser
import com.squareup.kotlinpoet.asTypeName

class MigrationParser : Parser<Migration, MigrationProperties> {
    override fun parse(input: Migration): MigrationProperties {
        return MigrationProperties(
            version = input.version,
            database = input.database.asTypeName(),
            priority = input.priority,
        )
    }
}
