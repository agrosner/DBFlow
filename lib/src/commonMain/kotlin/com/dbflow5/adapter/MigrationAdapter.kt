package com.dbflow5.adapter

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.database.scope.MigrationScope
import com.dbflow5.query.nameAlias
import com.dbflow5.query.operations.Property
import com.dbflow5.query.operations.property
import com.dbflow5.quoteIfNeeded
import com.dbflow5.sql.Query
import kotlin.reflect.KClass

/**
 * Creates a new [MigrationAdapter] to use dynamic table name and type in queries for migrations.
 * [ModelAdapter] use should be discouraged.
 */
@Suppress("unused")
fun MigrationScope.migrationAdapter(
    name: String
) = MigrationAdapter(name = name.quoteIfNeeded())

/**
 * Special class that enables migration-specific operations without need to use generated adapter
 * type. It only supports [FlowCursor] execution type, since we should not use model class types
 * in our queries, rather keep close to raw due to potential inconsistencies in migration state and
 * generated code state.
 */
data class MigrationAdapter internal constructor(
    override val name: String
) : WritableDBRepresentable<FlowCursor> {
    override val createWithDatabase: Boolean = false
    override val creationSQL: CompilableQuery
        get() = throw NotImplementedError("Method called in error. Use Raw query instead.")
    override val dropSQL: CompilableQuery = CompilableQuery("DROP TABLE IF EXISTS $name")

    override suspend fun DatabaseWrapper.single(query: Query): FlowCursor = rawQuery(query.query)

    /**
     * Returns a single list of [FlowCursor] to iterate query.
     */
    override suspend fun DatabaseWrapper.list(query: Query): List<FlowCursor> =
        listOf(rawQuery(query.query))

    override val type: KClass<FlowCursor> = FlowCursor::class

    override fun getProperty(columnName: String): Property<*, FlowCursor> =
        property<Any, FlowCursor>(columnName.nameAlias)
}