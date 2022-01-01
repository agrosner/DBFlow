package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.ReferenceProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class ForeignKeyReferencePropertyParser : Parser<KSAnnotation, ReferenceProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): ReferenceProperties {
        val args = input.arguments.mapProperties()
        val notNullArgs = args.arg<KSAnnotation>("notNull").arguments.mapProperties()
        return ReferenceProperties(
            name = args.arg("columnName"),
            referencedName = args.ifArg("foreignKeyColumnName") {
                arg(it)
            } ?: args.arg("columnMapFieldName"),
            defaultValue = args.arg("defaultValue"),
            onNullConflict = notNullArgs.enumArg("onNullConflict", ConflictAction::valueOf),
        )
    }
}