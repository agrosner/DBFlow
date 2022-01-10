package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.codegen.parser.validation.ValidationException
import com.dbflow5.codegen.model.properties.UniqueGroupProperties

class UniqueGroupPropertyParser : AnnotationParser<UniqueGroupProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): UniqueGroupProperties {
        return UniqueGroupProperties(
            number = arg("groupNumber"),
            conflictAction = enumArg("uniqueConflict", ConflictAction::valueOf)
        )
    }
}
