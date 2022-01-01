package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.UniqueProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class UniquePropertyParser : Parser<KSAnnotation, UniqueProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): UniqueProperties {
        val args = input.arguments.mapProperties()
        return UniqueProperties(
            unique = args.arg("unique"),
            groups = args.arg("uniqueGroups"),
            conflictAction = args.enumArg("onUniqueConflict", ConflictAction::valueOf)
        )
    }
}