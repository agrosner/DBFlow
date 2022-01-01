package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.ReferenceProperties
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.annotationMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.ksp.parser.ifArg
import com.dbflow5.ksp.parser.validation.ValidationException

class ForeignKeyReferencePropertyParser : AnnotationParser<ReferenceProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): ReferenceProperties {
        return ReferenceProperties(
            name = arg("columnName"),
            referencedName = ifArg("foreignKeyColumnName") {
                arg(it)
            } ?: arg("columnMapFieldName"),
            defaultValue = arg("defaultValue"),
            onNullConflict = annotationMap("notNull")
                .enumArg("onNullConflict", ConflictAction::valueOf),
        )
    }
}
