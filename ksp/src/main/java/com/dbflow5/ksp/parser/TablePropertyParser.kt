package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.TableProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class TablePropertyParser(
    private val indexGroupParser: IndexGroupParser,
    private val uniqueGroupPropertyParser: UniqueGroupPropertyParser,
) : Parser<KSAnnotation, TableProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): TableProperties {
        val args = input.arguments.mapProperties()
        return TableProperties(
            name = args.arg("name"),
            database = args.typeName("database"),
            allFields = args.arg("allFields"),
            orderedCursorLookup = args.arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = args.arg("assignDefaultValuesFromCursor"),
            createWithDatabase = args.arg("createWithDatabase"),
            updateConflict = args.enumArg("updateConflict", ConflictAction::valueOf),
            insertConflict = args.enumArg("insertConflict", ConflictAction::valueOf),
            primaryKeyConflict = args.enumArg("primaryKeyConflict", ConflictAction::valueOf),
            temporary = args.arg("temporary"),
            indexGroupProperties = args.arg<List<KSAnnotation>>("indexGroups")
                .map { indexGroupParser.parse(it) },
            uniqueGroupProperties = args.arg<List<KSAnnotation>>("uniqueColumnGroups")
                .map { uniqueGroupPropertyParser.parse(it) }
        )
    }
}