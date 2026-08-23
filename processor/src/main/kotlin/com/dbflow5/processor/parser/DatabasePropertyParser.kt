package com.dbflow5.processor.parser

import com.dbflow5.annotation.Database
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.properties.DatabaseProperties
import com.dbflow5.processor.utils.extractTypeNamesFromAnnotation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.javapoet.toKTypeName

class DatabasePropertyParser : Parser<Database, DatabaseProperties> {

    override fun parse(input: Database): DatabaseProperties {
        val tables =
            input.extractTypeNamesFromAnnotation { it.tables }.map { it.toKTypeName() as ClassName }
        val views =
            input.extractTypeNamesFromAnnotation { it.views }.map { it.toKTypeName() as ClassName }
        val queries =
            input.extractTypeNamesFromAnnotation { it.queries }
                .map { it.toKTypeName() as ClassName }
        return DatabaseProperties(
            version = input.version,
            foreignKeyConstraintsEnforced = input.foreignKeyConstraintsEnforced,
            updateConflict = input.updateConflict,
            insertConflict = input.insertConflict,
            tables = tables,
            views = views,
            queries = queries,
            classes = listOf(tables, views, queries).flatten(),
            migrations = input.extractTypeNamesFromAnnotation { it.migrations }
                .map { it.toKTypeName() as ClassName },
        )
    }
}