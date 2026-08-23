package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.properties.DatabaseProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.classNameList
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.ksp.parser.expectedArg

class DatabasePropertyParser : AnnotationParser<DatabaseProperties> {

    @Throws(ValidationException::class)
    override fun ArgMap.parse(): DatabaseProperties {
        val tables = classNameList("tables")
        val views = classNameList("views")
        val queries = classNameList("queries")
        return DatabaseProperties(
            version = expectedArg("version"),
            foreignKeyConstraintsEnforced = arg("foreignKeyConstraintsEnforced") ?: false,
            updateConflict = enumArg("updateConflict", ConflictAction.NONE, ConflictAction::valueOf),
            insertConflict = enumArg("insertConflict", ConflictAction.NONE, ConflictAction::valueOf),
            tables = tables,
            views = views,
            queries = queries,
            classes = listOf(tables, views, queries).flatten(),
            migrations = classNameList("migrations"),
        )
    }
}
