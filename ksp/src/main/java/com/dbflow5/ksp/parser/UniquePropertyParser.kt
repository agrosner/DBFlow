package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.UniqueProperties
import com.dbflow5.ksp.parser.validation.ValidationException

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
