package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.ReferenceProperties
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
