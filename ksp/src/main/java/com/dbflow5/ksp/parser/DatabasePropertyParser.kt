package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Collate
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.ksp.model.properties.DatabaseProperties
import com.dbflow5.ksp.model.properties.FieldProperties
import com.dbflow5.ksp.model.properties.ForeignKeyProperties
import com.dbflow5.ksp.model.properties.QueryProperties
import com.dbflow5.ksp.model.properties.ReferenceProperties
import com.dbflow5.ksp.model.properties.TableProperties
import com.dbflow5.ksp.model.properties.ViewProperties
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toTypeName

class DatabasePropertyParser : Parser<KSAnnotation, DatabaseProperties> {

    override fun parse(input: KSAnnotation): DatabaseProperties {
        val args = input.arguments.mapProperties()
        return DatabaseProperties(
            version = args.arg("version"),
            foreignKeyConstraintsEnforced = args.arg("foreignKeyConstraintsEnforced"),
            updateConflict = args.enumArg("updateConflict", ConflictAction::valueOf),
            insertConflict = args.enumArg("insertConflict", ConflictAction::valueOf),
        )
    }
}

class TablePropertyParser : Parser<KSAnnotation, TableProperties> {
    override fun parse(input: KSAnnotation): TableProperties {
        val args = input.arguments.mapProperties()
        return TableProperties(
            name = args.arg("name"),
            database = args.arg<KSType>("database").toTypeName(),
            allFields = args.arg("allFields"),
            orderedCursorLookup = args.arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = args.arg("assignDefaultValuesFromCursor"),
            createWithDatabase = args.arg("createWithDatabase"),
            updateConflict = args.enumArg("updateConflict", ConflictAction::valueOf),
            insertConflict = args.enumArg("insertConflict", ConflictAction::valueOf),
            primaryKeyConflict = args.enumArg("primaryKeyConflict", ConflictAction::valueOf),
            temporary = args.arg("temporary")
        )
    }
}

class ViewPropertyParser : Parser<KSAnnotation, ViewProperties> {
    override fun parse(input: KSAnnotation): ViewProperties {
        val args = input.arguments.mapProperties()
        return ViewProperties(
            name = args.arg("name"),
            database = args.arg<KSType>("database").toTypeName(),
            allFields = args.arg("allFields"),
            orderedCursorLookup = args.arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = args.arg("assignDefaultValuesFromCursor"),
            createWithDatabase = args.arg("createWithDatabase"),
        )
    }
}

class QueryPropertyParser : Parser<KSAnnotation, QueryProperties> {
    override fun parse(input: KSAnnotation): QueryProperties {
        val args = input.arguments.mapProperties()
        return QueryProperties(
            database = args.arg<KSType>("database").toTypeName(),
            allFields = args.arg("allFields"),
            orderedCursorLookup = args.arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = args.arg("assignDefaultValuesFromCursor"),
            createWithDatabase = args.arg("createWithDatabase"),
        )
    }
}

class FieldPropertyParser : Parser<KSAnnotation, FieldProperties> {
    override fun parse(input: KSAnnotation): FieldProperties {
        val args = input.arguments.mapProperties()
        return FieldProperties(
            name = args.arg("name"),
            length = args.arg("length"),
            collate = args.enumArg("collate", Collate::valueOf),
            defaultValue = args.arg("defaultValue"),
        )
    }
}

class ForeignKeyPropertyParser
constructor(
    private val foreignKeyReferencePropertyParser: ForeignKeyReferencePropertyParser,
) : Parser<KSAnnotation, ForeignKeyProperties> {
    override fun parse(input: KSAnnotation): ForeignKeyProperties {
        val args = input.arguments.mapProperties()
        val references = args.arg<List<KSAnnotation>>("references")
            .map { foreignKeyReferencePropertyParser.parse(it) }
        return ForeignKeyProperties(
            onDelete = args.enumArg("onDelete", ForeignKeyAction::valueOf),
            onUpdate = args.enumArg("onUpdate", ForeignKeyAction::valueOf),
            referencesType = when (references.isNotEmpty()) {
                true -> ForeignKeyProperties.ReferencesType.Specific(
                    references,
                )
                else -> ForeignKeyProperties.ReferencesType.All
            }
        )
    }
}

class ForeignKeyReferencePropertyParser : Parser<KSAnnotation, ReferenceProperties> {
    override fun parse(input: KSAnnotation): ReferenceProperties {
        val args = input.arguments.mapProperties()
        val notNullArgs = args.arg<KSAnnotation>("notNull").arguments.mapProperties()
        return ReferenceProperties(
            name = args.arg("name"),
            referencedName = args.arg("referencedName"),
            defaultValue = args.arg("defaultValue"),
            onNullConflict = notNullArgs.arg("onNullConflict"),
        )
    }
}