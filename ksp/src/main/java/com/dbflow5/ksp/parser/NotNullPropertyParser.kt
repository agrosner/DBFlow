package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.NotNullProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class NotNullPropertyParser : Parser<KSAnnotation, NotNullProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): NotNullProperties {
        val args = input.arguments.mapProperties()
        return NotNullProperties(
            conflictAction = args.enumArg("onNullConflict", ConflictAction::valueOf)
        )
    }
}