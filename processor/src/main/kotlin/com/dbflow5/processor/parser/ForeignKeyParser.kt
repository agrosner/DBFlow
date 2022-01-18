package com.dbflow5.processor.parser

import com.dbflow5.annotation.ColumnMap
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.codegen.shared.properties.ReferenceHolderProperties
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName

class ForeignKeyParser(
    private val foreignKeyReferencePropertyParser: ForeignKeyReferencePropertyParser,
) : Parser<ForeignKey,
    ReferenceHolderProperties> {
    override fun parse(input: ForeignKey): ReferenceHolderProperties {
        val references = input.references.map { foreignKeyReferencePropertyParser.parse(it) }
        return ReferenceHolderProperties(
            onDelete = input.onDelete,
            onUpdate = input.onUpdate,
            referencesType = when (references.isNotEmpty()) {
                true -> ReferenceHolderProperties.ReferencesType.Specific(
                    references,
                )
                else -> ReferenceHolderProperties.ReferencesType.All
            },
            referencedTableTypeName = input.extractTypeNameFromAnnotation { it.tableClass }
                .toKTypeName(),
            deferred = input.deferred,
            saveForeignKeyModel = input.saveForeignKeyModel,
        )
    }
}

class ColumnMapParser(
    private val columnMapReferencePropertyParser: ColumnMapReferencePropertyParser,
) : Parser<ColumnMap, ReferenceHolderProperties> {
    override fun parse(input: ColumnMap): ReferenceHolderProperties {
        val references = input.references.map { columnMapReferencePropertyParser.parse(it) }
        return ReferenceHolderProperties(
            onDelete = ForeignKeyAction.NO_ACTION,
            onUpdate = ForeignKeyAction.NO_ACTION,
            referencesType = when (references.isNotEmpty()) {
                true -> ReferenceHolderProperties.ReferencesType.Specific(
                    references,
                )
                else -> ReferenceHolderProperties.ReferencesType.All
            },
            referencedTableTypeName = Any::class.asTypeName(),
            deferred = false,
            saveForeignKeyModel = false,
        )
    }
}
