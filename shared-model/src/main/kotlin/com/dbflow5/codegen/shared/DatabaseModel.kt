package com.dbflow5.codegen.shared

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.properties.DatabaseProperties
import com.dbflow5.codegen.shared.properties.TableProperties
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class DatabaseModel(
    val name: NameModel,
    val classType: TypeName,
    val properties: DatabaseProperties,
    val tables: List<ClassModel> = listOf(),
    val views: List<ClassModel> = listOf(),
    val queries: List<ClassModel> = listOf(),
    val migrations: List<MigrationModel> = listOf(),
    override val originatingSource: OriginatingSource?,
) : ObjectModel

val DatabaseModel.generatedClassName
    get() = NameModel(
        packageName = name.packageName,
        shortName = "${name.shortName}_Database",
        nullable = false
    )

fun copyOverClasses(
    classes: List<ClassModel>,
    migrations: List<MigrationModel>,
): (DatabaseModel) -> DatabaseModel = { database ->
    database.copy(
        tables = classes.filter {
            it.partOfDatabaseAsType<ClassModel.Type.Table>(
                database.classType,
                database.properties.tables,
                database.properties.classes,
            )
        }.map { clazz ->
            // patch globular conflicts.
            (clazz.properties as? TableProperties)?.let { tableProperties ->
                clazz.copy(
                    properties = tableProperties
                        .copy(
                            insertConflict = tableProperties.insertConflict.takeIf { it != ConflictAction.NONE }
                                ?: database.properties.insertConflict,
                            updateConflict = tableProperties.updateConflict.takeIf { it != ConflictAction.NONE }
                                ?: database.properties.updateConflict
                        )
                )
            } ?: clazz
        },
        views = classes.filter {
            it.partOfDatabaseAsType<ClassModel.Type.View>(
                database.classType,
                database.properties.views,
                database.properties.classes,
            )
        },
        queries = classes.filter {
            it.partOfDatabaseAsType<ClassModel.Type.Query>(
                database.classType,
                database.properties.queries,
                database.properties.classes,
            )
        },
        migrations = migrations.filter { migration ->
            migration.properties.database == database.classType
                || database.properties.migrations.contains(migration.classType)
        },
    )
}

