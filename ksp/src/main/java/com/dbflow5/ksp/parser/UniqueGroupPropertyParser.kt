package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.UniqueGroupProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class UniqueGroupPropertyParser : AnnotationParser<UniqueGroupProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): UniqueGroupProperties {
        return UniqueGroupProperties(
            number = arg("groupNumber"),
            conflictAction = enumArg("uniqueConflict", ConflictAction::valueOf)
        )
    }
}
