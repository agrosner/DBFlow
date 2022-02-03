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
    val adapterFields: List<ClassAdapterFieldModel>,
    override val originatingSource: OriginatingSource?,
) : ObjectModel, GeneratedClassModel {
    override val generatedClassName: NameModel = NameModel(
        packageName = name.packageName,
        shortName = "${name.shortName}_Database",
        nullable = false
    )
}

fun copyOverClasses(
    classes: List<ClassModel>,
    migrations: List<MigrationModel>,
): (DatabaseModel) -> DatabaseModel = { database ->
    val tables = classes.filter {
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
    }
    val views = classes.filter {
        it.partOfDatabaseAsType<ClassModel.Type.View>(
            database.classType,
            database.properties.views,
            database.properties.classes,
        )
    }
    val queries = classes.filter {
        it.partOfDatabaseAsType<ClassModel.Type.Query>(
            database.classType,
            database.properties.queries,
            database.properties.classes,
        )
    }
    val primedAdapterFields = primeAdapterFields(database, tables, queries, views)
    database.copy(
        tables = tables,
        views = views,
        queries = queries,
        migrations = migrations.filter { migration ->
            migration.properties.database == database.classType
                || database.properties.migrations.contains(migration.classType)
        },
        adapterFields = primedAdapterFields,
    )
}

private fun primeAdapterFields(
    database: DatabaseModel,
    tables: List<ClassModel>,
    queries: List<ClassModel>,
    views: List<ClassModel>
) = database.adapterFields
    .map { fieldModel ->
        when (fieldModel.type) {
            ClassAdapterFieldModel.Type.Normal -> tables.first {
                fieldModel.associateClassModel(it)
            }
            ClassAdapterFieldModel.Type.Query -> queries.first {
                fieldModel.associateClassModel(it)
            }
            ClassAdapterFieldModel.Type.View -> views.first {
                fieldModel.associateClassModel(it)
            }
        }
        fieldModel
    }

