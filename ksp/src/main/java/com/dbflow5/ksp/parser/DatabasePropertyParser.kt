package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.DatabaseProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class DatabasePropertyParser : AnnotationParser<DatabaseProperties> {

    @Throws(ValidationException::class)
    override fun ArgMap.parse(): DatabaseProperties {
        return DatabaseProperties(
            version = arg("version"),
            foreignKeyConstraintsEnforced = arg("foreignKeyConstraintsEnforced"),
            updateConflict = enumArg("updateConflict", ConflictAction::valueOf),
            insertConflict = enumArg("insertConflict", ConflictAction::valueOf),
            areConsistencyChecksEnabled = arg("consistencyCheckEnabled"),
            backupEnabled = arg("backupEnabled"),
        )
    }
}
