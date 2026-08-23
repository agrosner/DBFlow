package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.database.DatabaseObjectLookup
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.query.operations.Property
import com.dbflow5.sql.Query
import kotlin.reflect.KClass

@InternalDBFlowApi
fun <T : Any> makeLazyDBRepresentable(
    sqlObject: KClass<T>,
): WritableDBRepresentable<T> = LazyDBRepresentable(sqlObject)

/**
 * Used by inner library classes.
 * Lazily looks up sqlObject from [DatabaseObjectLookup] to get around initialization on top-level
 * Properties or classes that need an adapter.
 */
internal class LazyDBRepresentable<T : Any>(
    sqlObject: KClass<T>
) : WritableDBRepresentable<T> {
    private val realAdapter by lazy { DatabaseObjectLookup.getDBRepresentable(sqlObject) }
    override val type: KClass<T>
        get() = realAdapter.type
    override val name: String
        get() = realAdapter.name
    override val creationSQL: CompilableQuery
        get() = realAdapter.creationSQL
    override val createWithDatabase: Boolean
        get() = realAdapter.createWithDatabase

    override val dropSQL: CompilableQuery
        get() = realAdapter.dropSQL

    override fun getProperty(columnName: String): Property<*, T> =
        realAdapter.getProperty(columnName)

    override suspend fun DatabaseConnection.single(query: Query): T? =
        realAdapter.run { single(query) }

    override suspend fun DatabaseConnection.list(query: Query): List<T> =
        realAdapter.run { list(query) }
}
