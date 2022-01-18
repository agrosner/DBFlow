package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.codegen.shared.parser.validation.ValidationException
import com.dbflow5.codegen.shared.properties.UniqueProperties

class UniquePropertyParser : AnnotationParser<UniqueProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): UniqueProperties {
        return UniqueProperties(
            unique = arg("unique"),
            groups = arg("uniqueGroups"),
            conflictAction = enumArg("onUniqueConflict", ConflictAction::valueOf)
        )
    }
}
