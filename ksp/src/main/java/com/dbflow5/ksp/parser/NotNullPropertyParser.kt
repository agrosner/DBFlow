package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.NotNullProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class NotNullPropertyParser : AnnotationParser<NotNullProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): NotNullProperties {
        return NotNullProperties(
            conflictAction = enumArg("onNullConflict", ConflictAction::valueOf)
        )
    }
}
