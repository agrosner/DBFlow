package com.dbflow5.processor.parser

import com.dbflow5.annotation.Database
import com.dbflow5.codegen.shared.properties.DatabaseProperties
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.processor.utils.extractClassNamesFromAnnotation
import com.squareup.kotlinpoet.javapoet.toKClassName

class DatabasePropertyParser : Parser<Database, DatabaseProperties> {

    override fun parse(input: Database): DatabaseProperties {
        val tables = input.extractClassNamesFromAnnotation { it.tables }.map { it.toKClassName() }
        val views = input.extractClassNamesFromAnnotation { it.views }.map { it.toKClassName() }
        val queries =
            input.extractClassNamesFromAnnotation { it.queries }.map { it.toKClassName() }
        return DatabaseProperties(
            version = input.version,
            foreignKeyConstraintsEnforced = input.foreignKeyConstraintsEnforced,
            updateConflict = input.updateConflict,
            insertConflict = input.insertConflict,
            tables = tables,
            views = views,
            queries = queries,
            classes = listOf(tables, views, queries).flatten(),
            migrations = input.extractClassNamesFromAnnotation { it.migrations }
                .map { it.toKClassName() },
        )
    }
}