package com.dbflow5.ksp.parser.annotation

import com.dbflow5.ksp.model.properties.ManyToManyProperties
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.className
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
