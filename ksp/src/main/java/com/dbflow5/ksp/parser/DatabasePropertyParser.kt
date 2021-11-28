package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.DatabaseModel
import com.google.devtools.ksp.symbol.KSAnnotation

class DatabasePropertyParser : Parser<KSAnnotation, DatabaseModel.Properties> {

    override fun parse(input: KSAnnotation): DatabaseModel.Properties {
        val args = input.arguments.mapProperties()
        return DatabaseModel.Properties(
            version = args.arg("version"),
            foreignKeyConstraintsEnforced = args.arg("foreignKeyConstraintsEnforced"),
            insertConflict = ConflictAction.valueOf(args.arg("insertConflict")),
            updateConflict = ConflictAction.valueOf(args.arg("updateConflict"))
        )
    }
}