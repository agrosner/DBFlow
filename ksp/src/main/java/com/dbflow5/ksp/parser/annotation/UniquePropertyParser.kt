package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.properties.UniqueProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.enumArg

class UniquePropertyParser : AnnotationParser<UniqueProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): UniqueProperties {
        return UniqueProperties(
            unique = arg("unique") ?: true,
            groups = arg("uniqueGroups") ?: listOf(),
            conflictAction = enumArg("onUniqueConflict", ConflictAction.FAIL, ConflictAction::valueOf)
        )
    }
}
