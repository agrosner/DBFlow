package com.dbflow5.processor.parser

import com.dbflow5.annotation.ManyToMany
import com.dbflow5.codegen.model.properties.ManyToManyProperties
import com.dbflow5.codegen.parser.Parser
import com.squareup.kotlinpoet.asTypeName

class ManyToManyPropertyParser : Parser<ManyToMany, ManyToManyProperties> {
    override fun parse(input: ManyToMany): ManyToManyProperties {
        return ManyToManyProperties(
            referencedTableType = input.referencedTable.asTypeName(),
            referencedTableColumnName = input.referencedTableColumnName,
            thisTableColumnName = input.thisTableColumnName,
            generateAutoIncrement = input.generateAutoIncrement,
            saveForeignKeyModels = input.saveForeignKeyModels,
            name = input.generatedTableClassName,
        )
    }
}
