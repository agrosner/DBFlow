package com.dbflow5.processor.parser

import com.dbflow5.annotation.Table
import com.dbflow5.codegen.model.properties.TableProperties
import com.dbflow5.codegen.parser.Parser
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.squareup.kotlinpoet.javapoet.toKTypeName

class TablePropertyParser(
    private val indexGroupParser: IndexGroupParser,
    private val uniqueGroupPropertyParser: UniqueGroupPropertyParser,
) : Parser<Table, TableProperties> {
    override fun parse(input: Table): TableProperties {
        return TableProperties(
            name = input.name,
            database = input.extractTypeNameFromAnnotation { it.database }.toKTypeName(),
            allFields = input.allFields,
            orderedCursorLookup = input.orderedCursorLookUp,
            assignDefaultValuesFromCursor = input.assignDefaultValuesFromCursor,
            createWithDatabase = input.createWithDatabase,
            updateConflict = input.updateConflict,
            insertConflict = input.insertConflict,
            primaryKeyConflict = input.primaryKeyConflict,
            temporary = input.temporary,
            indexGroupProperties = input.indexGroups.map { indexGroupParser.parse(it) },
            uniqueGroupProperties = input.uniqueColumnGroups.map {
                uniqueGroupPropertyParser.parse(
                    it
                )
            },
        )
    }
}
