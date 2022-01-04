package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.DatabaseProperties
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.classNameList
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.ksp.parser.validation.ValidationException

class DatabasePropertyParser : AnnotationParser<DatabaseProperties> {

    @Throws(ValidationException::class)
    override fun ArgMap.parse(): DatabaseProperties {
        return DatabaseProperties(
            version = arg("version"),
            foreignKeyConstraintsEnforced = arg("foreignKeyConstraintsEnforced"),
            updateConflict = enumArg("updateConflict", ConflictAction::valueOf),
            insertConflict = enumArg("insertConflict", ConflictAction::valueOf),
            tables = classNameList("tables"),
            views = classNameList("views"),
            queries = classNameList("queries"),
            migrations = classNameList("migrations"),
        )
    }
}
