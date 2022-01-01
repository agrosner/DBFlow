package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.properties.ManyToManyProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class ManyToManyPropertyParser : Parser<KSAnnotation, ManyToManyProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): ManyToManyProperties {
        val args = input.arguments.mapProperties()
        return ManyToManyProperties(
            referencedTableType = args.className("referencedTable"),
            referencedTableColumnName = args.arg("referencedTableColumnName"),
            thisTableColumnName = args.arg("thisTableColumnName"),
            generateAutoIncrement = args.arg("generateAutoIncrement"),
            saveForeignKeyModels = args.arg("saveForeignKeyModels"),
            name = args.arg("generatedTableClassName"),
        )
    }
}