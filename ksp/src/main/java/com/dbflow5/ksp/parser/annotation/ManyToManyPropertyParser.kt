package com.dbflow5.ksp.parser.annotation

import com.dbflow5.codegen.shared.properties.ManyToManyProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.className

class ManyToManyPropertyParser : AnnotationParser<ManyToManyProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): ManyToManyProperties {
        return ManyToManyProperties(
            referencedTableType = className("referencedTable")!!,
            referencedTableColumnName = arg("referencedTableColumnName") ?: "",
            thisTableColumnName = arg("thisTableColumnName") ?: "",
            generateAutoIncrement = arg("generateAutoIncrement") ?: true,
            saveForeignKeyModels = arg("saveForeignKeyModels") ?: false,
            name = arg("generatedTableClassName") ?: "",
        )
    }
}
