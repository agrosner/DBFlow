package com.dbflow5.ksp.parser

import com.dbflow5.annotation.Collate
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.properties.DatabaseProperties
import com.dbflow5.ksp.model.properties.FieldProperties
import com.dbflow5.ksp.model.properties.IndexGroupProperties
import com.dbflow5.ksp.model.properties.IndexProperties
import com.dbflow5.ksp.model.properties.ManyToManyProperties
import com.dbflow5.ksp.model.properties.MigrationProperties
import com.dbflow5.ksp.model.properties.NotNullProperties
import com.dbflow5.ksp.model.properties.OneToManyProperties
import com.dbflow5.ksp.model.properties.QueryProperties
import com.dbflow5.ksp.model.properties.ReferenceHolderProperties
import com.dbflow5.ksp.model.properties.ReferenceProperties
import com.dbflow5.ksp.model.properties.TableProperties
import com.dbflow5.ksp.model.properties.UniqueGroupProperties
import com.dbflow5.ksp.model.properties.UniqueProperties
import com.dbflow5.ksp.model.properties.ViewProperties
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.asTypeName

class DatabasePropertyParser : Parser<KSAnnotation, DatabaseProperties> {

    override fun parse(input: KSAnnotation): DatabaseProperties {
        val args = input.arguments.mapProperties()
        return DatabaseProperties(
            version = args.arg("version"),
            foreignKeyConstraintsEnforced = args.arg("foreignKeyConstraintsEnforced"),
            updateConflict = args.enumArg("updateConflict", ConflictAction::valueOf),
            insertConflict = args.enumArg("insertConflict", ConflictAction::valueOf),
            areConsistencyChecksEnabled = args.arg("consistencyCheckEnabled"),
            backupEnabled = args.arg("backupEnabled"),
        )
    }
}

class TablePropertyParser(
    private val indexGroupParser: IndexGroupParser,
    private val uniqueGroupPropertyParser: UniqueGroupPropertyParser,
) : Parser<KSAnnotation, TableProperties> {
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

class ViewPropertyParser : Parser<KSAnnotation, ViewProperties> {

    override fun parse(input: KSAnnotation): ViewProperties {
        val args = input.arguments.mapProperties()
        return ViewProperties(
            name = args.arg("name"),
            database = args.typeName("database"),
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
            database = args.typeName("database"),
            allFields = args.arg("allFields"),
            orderedCursorLookup = args.arg("orderedCursorLookUp"),
            assignDefaultValuesFromCursor = args.arg("assignDefaultValuesFromCursor"),
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
            typeConverterClassName = args.className("typeConverter"),
        )
    }
}

class ReferenceHolderProperyParser
constructor(
    private val foreignKeyReferencePropertyParser: ForeignKeyReferencePropertyParser,
) : Parser<KSAnnotation, ReferenceHolderProperties> {
    override fun parse(input: KSAnnotation): ReferenceHolderProperties {
        val args = input.arguments.mapProperties()
        val references = args.arg<List<KSAnnotation>>("references")
            .map { foreignKeyReferencePropertyParser.parse(it) }
        return ReferenceHolderProperties(
            onDelete = args.ifArg("onDelete") {
                enumArg(it, ForeignKeyAction::valueOf)
            } ?: ForeignKeyAction.NO_ACTION,
            onUpdate = args.ifArg("onUpdate") {
                enumArg("onUpdate", ForeignKeyAction::valueOf)
            } ?: ForeignKeyAction.NO_ACTION,
            referencesType = when (references.isNotEmpty()) {
                true -> ReferenceHolderProperties.ReferencesType.Specific(
                    references,
                )
                else -> ReferenceHolderProperties.ReferencesType.All
            },
            referencedTableTypeName = args.ifArg("tableClass") {
                className("tableClass")
            } ?: Any::class.asTypeName()
        )
    }
}

class ForeignKeyReferencePropertyParser : Parser<KSAnnotation, ReferenceProperties> {
    override fun parse(input: KSAnnotation): ReferenceProperties {
        val args = input.arguments.mapProperties()
        val notNullArgs = args.arg<KSAnnotation>("notNull").arguments.mapProperties()
        return ReferenceProperties(
            name = args.arg("columnName"),
            referencedName = args.ifArg("foreignKeyColumnName") {
                arg(it)
            } ?: args.arg("columnMapFieldName"),
            defaultValue = args.arg("defaultValue"),
            onNullConflict = notNullArgs.enumArg("onNullConflict", ConflictAction::valueOf),
        )
    }
}

class ManyToManyPropertyParser : Parser<KSAnnotation, ManyToManyProperties> {
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

class OneToManyPropertyParser : Parser<KSAnnotation, OneToManyProperties> {
    override fun parse(input: KSAnnotation): OneToManyProperties {
        val args = input.arguments.mapProperties()
        return OneToManyProperties(
            childTableType = args.className("childTable"),
            name = args.arg("generatedClassName"),
            parentFieldName = args.arg("parentFieldName"),
            childListFieldName = args.arg("childListFieldName"),
        )
    }
}

class Fts4Parser : Parser<KSAnnotation, ClassModel.ClassType.Normal.Fts4> {
    override fun parse(input: KSAnnotation): ClassModel.ClassType.Normal.Fts4 {
        val args = input.arguments.mapProperties()
        return ClassModel.ClassType.Normal.Fts4(
            contentTable = args.typeName("contentTable"),
        )
    }
}

class IndexParser : Parser<KSAnnotation, IndexProperties> {
    override fun parse(input: KSAnnotation): IndexProperties {
        val args = input.arguments.mapProperties()
        return IndexProperties(
            groups = args.arg("indexGroups")
        )
    }
}

class IndexGroupParser : Parser<KSAnnotation, IndexGroupProperties> {
    override fun parse(input: KSAnnotation): IndexGroupProperties {
        val args = input.arguments.mapProperties()
        return IndexGroupProperties(
            number = args.arg("number"),
            name = args.arg("name"),
            unique = args.arg("unique"),
        )
    }
}

class NotNullPropertyParser : Parser<KSAnnotation, NotNullProperties> {
    override fun parse(input: KSAnnotation): NotNullProperties {
        val args = input.arguments.mapProperties()
        return NotNullProperties(
            conflictAction = args.enumArg("onNullConflict", ConflictAction::valueOf)
        )
    }
}

class UniqueGroupPropertyParser : Parser<KSAnnotation, UniqueGroupProperties> {
    override fun parse(input: KSAnnotation): UniqueGroupProperties {
        val args = input.arguments.mapProperties()
        return UniqueGroupProperties(
            number = args.arg("groupNumber"),
            conflictAction = args.enumArg("uniqueConflict", ConflictAction::valueOf)
        )
    }
}

class UniquePropertyParser : Parser<KSAnnotation, UniqueProperties> {
    override fun parse(input: KSAnnotation): UniqueProperties {
        val args = input.arguments.mapProperties()
        return UniqueProperties(
            unique = args.arg("unique"),
            groups = args.arg("uniqueGroups"),
            conflictAction = args.enumArg("onUniqueConflict", ConflictAction::valueOf)
        )
    }
}

class MigrationParser : Parser<KSAnnotation, MigrationProperties> {
    override fun parse(input: KSAnnotation): MigrationProperties {
        val args = input.arguments.mapProperties()
        return MigrationProperties(
            version = args.arg("version"),
            database = args.typeName("database"),
            priority = args.arg("priority"),
        )
    }
}