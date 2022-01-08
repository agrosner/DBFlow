package com.dbflow5.ksp.model

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.properties.DatabaseProperties
import com.dbflow5.ksp.model.properties.TableProperties
import com.google.devtools.ksp.symbol.KSFile
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
    override val originatingFile: KSFile?,
) : ObjectModel

val DatabaseModel.generatedClassName
    get() = NameModel(
        name.packageName,
        "${name.shortName}_Database"
    )

fun copyOverClasses(
    classes: List<ClassModel>,
    migrations: List<MigrationModel>,
): (DatabaseModel) -> DatabaseModel = { database ->
    database.copy(
        tables = classes.filter {
            it.partOfDatabaseAsType<ClassModel.ClassType.Normal>(
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
            it.partOfDatabaseAsType<ClassModel.ClassType.View>(
                database.classType,
                database.properties.views,
                database.properties.classes,
            )
        },
        queries = classes.filter {
            it.partOfDatabaseAsType<ClassModel.ClassType.Query>(
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

