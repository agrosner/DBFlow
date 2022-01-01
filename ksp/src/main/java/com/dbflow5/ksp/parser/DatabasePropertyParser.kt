package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.DatabaseProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation

class DatabasePropertyParser : Parser<KSAnnotation, DatabaseProperties> {

    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): DatabaseProperties {
        val args = input.arguments.mapProperties()
        return DatabaseProperties(
            version = args.arg("version"),
            foreignKeyConstraintsEnforced = args.arg("foreignKeyConstraintsEnforced"),
            updateConflict = args.enumArg("updateConflict", ConflictAction::valueOf),
            insertConflict = args.enumArg("insertConflict", ConflictAction::valueOf),
            areConsistencyChecksEnabled = args.arg("consistencyCheckEnabled"),
            backupEnabled = args.arg("backupEnabled"),
        )
    }
}
