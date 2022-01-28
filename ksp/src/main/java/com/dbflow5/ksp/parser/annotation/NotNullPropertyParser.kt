package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.codegen.shared.properties.NotNullProperties

class NotNullPropertyParser : AnnotationParser<NotNullProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): NotNullProperties {
        return NotNullProperties(
            conflictAction = enumArg("onNullConflict", ConflictAction::valueOf)
        )
    }
}
