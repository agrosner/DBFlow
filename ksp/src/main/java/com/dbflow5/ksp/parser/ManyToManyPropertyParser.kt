package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.ManyToManyProperties
import com.dbflow5.ksp.parser.validation.ValidationException

class ManyToManyPropertyParser : AnnotationParser<ManyToManyProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): ManyToManyProperties {
        return ManyToManyProperties(
            referencedTableType = className("referencedTable"),
            referencedTableColumnName = arg("referencedTableColumnName"),
            thisTableColumnName = arg("thisTableColumnName"),
            generateAutoIncrement = arg("generateAutoIncrement"),
            saveForeignKeyModels = arg("saveForeignKeyModels"),
            name = arg("generatedTableClassName"),
        )
    }
}
