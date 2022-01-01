package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.UniqueGroupProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class UniqueGroupPropertyParser : Parser<KSAnnotation, UniqueGroupProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): UniqueGroupProperties {
        val args = input.arguments.mapProperties()
        return UniqueGroupProperties(
            number = args.arg("groupNumber"),
            conflictAction = args.enumArg("uniqueConflict", ConflictAction::valueOf)
        )
    }
}