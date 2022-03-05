package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.properties.TableProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.ksp.parser.typeName
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.asTypeName

class TablePropertyParser(
    private val indexGroupParser: IndexGroupParser,
    private val uniqueGroupPropertyParser: UniqueGroupPropertyParser,
) : AnnotationParser<TableProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): TableProperties {
        return TableProperties(
            name = arg("name") ?: "",
            database = typeName("database") ?: Any::class.asTypeName(),
            allFields = arg("allFields") ?: true,
            orderedCursorLookup = arg("orderedCursorLookUp") ?: false,
            assignDefaultValuesFromCursor = arg("assignDefaultValuesFromCursor") ?: true,
            createWithDatabase = arg("createWithDatabase") ?: true,
            updateConflict = enumArg("updateConflict", ConflictAction.NONE, ConflictAction::valueOf),
            insertConflict = enumArg("insertConflict", ConflictAction.NONE, ConflictAction::valueOf),
            primaryKeyConflict = enumArg("primaryKeyConflict", ConflictAction.NONE, ConflictAction::valueOf),
            temporary = arg("temporary") ?: false,
            indexGroupProperties = arg<List<KSAnnotation>?>("indexGroups")
                ?.map { indexGroupParser.parse(it) } ?: listOf(),
            uniqueGroupProperties = arg<List<KSAnnotation>?>("uniqueColumnGroups")
                ?.map { uniqueGroupPropertyParser.parse(it) } ?: listOf(),
        )
    }
}
