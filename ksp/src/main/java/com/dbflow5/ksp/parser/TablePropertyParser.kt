package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.TableProperties
import com.dbflow5.ksp.parser.validation.ValidationException
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
