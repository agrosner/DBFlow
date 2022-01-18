package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.ksp.parser.typeName
import com.dbflow5.codegen.shared.parser.validation.ValidationException
import com.dbflow5.codegen.shared.properties.TableProperties
import com.google.devtools.ksp.symbol.KSAnnotation

class TablePropertyParser(
    private val indexGroupParser: IndexGroupParser,
    private val uniqueGroupPropertyParser: UniqueGroupPropertyParser,
) : AnnotationParser<TableProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): TableProperties {
        return TableProperties(
            name = arg("name"),
            database = typeName("database"),
            allFields = arg("allFields"),
            orderedCursorLookup = arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = arg("assignDefaultValuesFromCursor"),
            createWithDatabase = arg("createWithDatabase"),
            updateConflict = enumArg("updateConflict", ConflictAction::valueOf),
            insertConflict = enumArg("insertConflict", ConflictAction::valueOf),
            primaryKeyConflict = enumArg("primaryKeyConflict", ConflictAction::valueOf),
            temporary = arg("temporary"),
            indexGroupProperties = arg<List<KSAnnotation>>("indexGroups")
                .map { indexGroupParser.parse(it) },
            uniqueGroupProperties = arg<List<KSAnnotation>>("uniqueColumnGroups")
                .map { uniqueGroupPropertyParser.parse(it) }
        )
    }
}
