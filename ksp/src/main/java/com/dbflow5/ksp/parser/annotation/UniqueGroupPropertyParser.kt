package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.properties.UniqueGroupProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.ksp.parser.expectedArg

class UniqueGroupPropertyParser : AnnotationParser<UniqueGroupProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): UniqueGroupProperties {
        return UniqueGroupProperties(
            number = expectedArg("groupNumber"),
            conflictAction = enumArg("uniqueConflict", ConflictAction.FAIL, ConflictAction::valueOf)
        )
    }
}
