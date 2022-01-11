package com.dbflow5.processor.parser

import com.dbflow5.annotation.ColumnMapReference
import com.dbflow5.annotation.ForeignKeyReference
import com.dbflow5.codegen.model.properties.ReferenceProperties
import com.dbflow5.codegen.parser.Parser

/**
 * Description:
 */
class ForeignKeyReferencePropertyParser : Parser<ForeignKeyReference,
    ReferenceProperties> {
    override fun parse(input: ForeignKeyReference): ReferenceProperties {
        return ReferenceProperties(
            name = input.columnName,
            referencedName = input.foreignKeyColumnName,
            defaultValue = input.defaultValue,
            onNullConflict = input.notNull.onNullConflict,
        )
    }
}

class ColumnMapReferencePropertyParser : Parser<ColumnMapReference,
    ReferenceProperties> {
    override fun parse(input: ColumnMapReference): ReferenceProperties {
        return ReferenceProperties(
            name = input.columnName,
            referencedName = input.columnMapFieldName,
            defaultValue = input.defaultValue,
            onNullConflict = input.notNull.onNullConflict,
        )
    }
}
