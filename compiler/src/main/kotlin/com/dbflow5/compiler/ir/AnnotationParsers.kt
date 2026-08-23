package com.dbflow5.compiler.ir

import com.dbflow5.annotation.Collate
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.annotation.INDEX_GENERIC
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.properties.DatabaseProperties
import com.dbflow5.codegen.shared.properties.FieldProperties
import com.dbflow5.codegen.shared.properties.IndexGroupProperties
import com.dbflow5.codegen.shared.properties.IndexProperties
import com.dbflow5.codegen.shared.properties.ManyToManyProperties
import com.dbflow5.codegen.shared.properties.MigrationProperties
import com.dbflow5.codegen.shared.properties.NotNullProperties
import com.dbflow5.codegen.shared.properties.OneToManyProperties
import com.dbflow5.codegen.shared.properties.QueryProperties
import com.dbflow5.codegen.shared.properties.ReferenceHolderProperties
import com.dbflow5.codegen.shared.properties.ReferenceProperties
import com.dbflow5.codegen.shared.properties.TableProperties
import com.dbflow5.codegen.shared.properties.TypeConverterProperties
import com.dbflow5.codegen.shared.properties.UniqueGroupProperties
import com.dbflow5.codegen.shared.properties.UniqueProperties
import com.dbflow5.codegen.shared.properties.ViewProperties
import com.dbflow5.converter.TypeConverter
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

internal fun AnnotationMap.toDatabaseProperties(): DatabaseProperties {
    val tables = classNameList("tables")
    val views = classNameList("views")
    val queries = classNameList("queries")
    return DatabaseProperties(
        version = expectedArg("version"),
        foreignKeyConstraintsEnforced = arg("foreignKeyConstraintsEnforced") ?: false,
        updateConflict = enumArg("updateConflict", ConflictAction.NONE, ConflictAction::valueOf),
        insertConflict = enumArg("insertConflict", ConflictAction.NONE, ConflictAction::valueOf),
        tables = tables,
        views = views,
        queries = queries,
        classes = listOf(tables, views, queries).flatten(),
        migrations = classNameList("migrations"),
    )
}

internal fun AnnotationMap.toTableProperties(): TableProperties = TableProperties(
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
    indexGroupProperties = nestedList("indexGroups").map { it.toIndexGroupProperties() },
    uniqueGroupProperties = nestedList("uniqueColumnGroups").map { it.toUniqueGroupProperties() },
)

internal fun AnnotationMap.toQueryProperties(): QueryProperties = QueryProperties(
    database = typeName("database") ?: Any::class.asTypeName(),
    allFields = arg("allFields") ?: true,
    orderedCursorLookup = arg("orderedCursorLookUp") ?: false,
    assignDefaultValuesFromCursor = arg("assignDefaultValuesFromCursor") ?: true,
)

internal fun AnnotationMap.toViewProperties(): ViewProperties = ViewProperties(
    name = arg("name") ?: "",
    database = typeName("database") ?: Any::class.asTypeName(),
    allFields = arg("allFields") ?: true,
    orderedCursorLookup = arg("orderedCursorLookUp") ?: false,
    assignDefaultValuesFromCursor = arg("assignDefaultValuesFromCursor") ?: true,
    createWithDatabase = arg("createWithDatabase") ?: true,
)

internal fun AnnotationMap.toTypeConverterProperties(): TypeConverterProperties =
    TypeConverterProperties(
        allowedSubtypeTypeNames = (get("allowedSubtypes") as? List<*>)
            ?.mapNotNull { it as? TypeName }
            ?: emptyList(),
    )

internal fun AnnotationMap.toManyToManyProperties(): ManyToManyProperties = ManyToManyProperties(
    referencedTableType = className("referencedTable")!!,
    referencedTableColumnName = arg("referencedTableColumnName") ?: "",
    thisTableColumnName = arg("thisTableColumnName") ?: "",
    generateAutoIncrement = arg("generateAutoIncrement") ?: true,
    saveForeignKeyModels = arg("saveForeignKeyModels") ?: false,
    name = arg("generatedTableClassName") ?: "",
)

internal fun AnnotationMap.toOneToManyProperties(): OneToManyProperties = OneToManyProperties(
    childTableType = className("childTable")!!,
    name = arg("generatedClassName") ?: "",
    parentFieldName = arg("parentFieldName") ?: "",
    childListFieldName = arg("childListFieldName") ?: "children",
)

internal fun AnnotationMap.toMigrationProperties(): MigrationProperties = MigrationProperties(
    version = expectedArg("version"),
    database = typeName("database") ?: Any::class.asTypeName(),
    priority = arg("priority") ?: -1,
)

internal fun AnnotationMap.toFts4Type(): ClassModel.Type.Table.Fts4 = ClassModel.Type.Table.Fts4(
    contentTable = typeName("contentTable") ?: Any::class.asTypeName(),
)

internal fun AnnotationMap.toFieldProperties(): FieldProperties = FieldProperties(
    name = arg("name") ?: "",
    length = arg("length") ?: -1,
    collate = enumArg("collate", Collate.None, Collate::valueOf),
    defaultValue = arg("defaultValue") ?: "",
    typeConverterClassName = className("typeConverter") ?: TypeConverter::class.asClassName(),
)

internal fun AnnotationMap.toReferenceHolderProperties(): ReferenceHolderProperties {
    val references = nestedList("references").map { it.toReferenceProperties() }
    return ReferenceHolderProperties(
        onDelete = enumArg("onDelete", ForeignKeyAction.NO_ACTION, ForeignKeyAction::valueOf),
        onUpdate = enumArg("onUpdate", ForeignKeyAction.NO_ACTION, ForeignKeyAction::valueOf),
        referencesType = if (references.isNotEmpty()) {
            ReferenceHolderProperties.ReferencesType.Specific(references)
        } else {
            ReferenceHolderProperties.ReferencesType.All
        },
        referencedTableTypeName = className("tableClass") ?: Any::class.asTypeName(),
        deferred = arg("deferred") ?: false,
        saveForeignKeyModel = arg("saveForeignKeyModel") ?: false,
    )
}

internal fun AnnotationMap.toReferenceProperties(): ReferenceProperties = ReferenceProperties(
    name = arg("columnName") ?: "",
    referencedName = arg("foreignKeyColumnName") ?: arg("columnMapFieldName") ?: "",
    defaultValue = arg("defaultValue") ?: "",
    onNullConflict = nested("notNull")
        .enumArg("onNullConflict", ConflictAction.NONE, ConflictAction::valueOf),
)

internal fun AnnotationMap.toIndexProperties(): IndexProperties = IndexProperties(
    groups = arg("indexGroups") ?: emptyList(),
)

internal fun AnnotationMap.toIndexGroupProperties(): IndexGroupProperties = IndexGroupProperties(
    number = arg("number") ?: INDEX_GENERIC,
    name = expectedArg("name"),
    unique = arg("unique") ?: false,
)

internal fun AnnotationMap.toUniqueProperties(): UniqueProperties = UniqueProperties(
    unique = arg("unique") ?: true,
    groups = arg("uniqueGroups") ?: emptyList(),
    conflictAction = enumArg("onUniqueConflict", ConflictAction.FAIL, ConflictAction::valueOf),
)

internal fun AnnotationMap.toUniqueGroupProperties(): UniqueGroupProperties = UniqueGroupProperties(
    number = expectedArg("groupNumber"),
    conflictAction = enumArg("uniqueConflict", ConflictAction.FAIL, ConflictAction::valueOf),
)

internal fun AnnotationMap.toNotNullProperties(): NotNullProperties = NotNullProperties(
    conflictAction = enumArg("onNullConflict", ConflictAction.FAIL, ConflictAction::valueOf),
)
