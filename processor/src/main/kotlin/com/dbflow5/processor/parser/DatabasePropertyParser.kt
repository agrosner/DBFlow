package com.dbflow5.processor.parser

import com.dbflow5.annotation.Database
import com.dbflow5.codegen.model.properties.DatabaseProperties
import com.dbflow5.codegen.parser.Parser
import com.squareup.kotlinpoet.asClassName

class DatabasePropertyParser : Parser<Database, DatabaseProperties> {

    override fun parse(input: Database): DatabaseProperties {
        val tables = input.tables.map { it.asClassName() }
        val views = input.views.map { it.asClassName() }
        val queries = input.queries.map { it.asClassName() }
        return DatabaseProperties(
            version = input.version,
            foreignKeyConstraintsEnforced = input.foreignKeyConstraintsEnforced,
            updateConflict = input.updateConflict,
            insertConflict = input.insertConflict,
            tables = tables,
            views = views,
            queries = queries,
            classes = listOf(tables, views, queries).flatten(),
            migrations = input.migrations.map { it.asClassName() },
        )
    }
}