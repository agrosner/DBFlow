package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.config.FlowManager
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.sql.Query
import kotlin.reflect.KClass

@InternalDBFlowApi
fun <T : Any> makeLazyDBRepresentable(
    sqlObject: KClass<T>,
): DBRepresentable<T> = LazyDBRepresentable(sqlObject)

/**
 * Used by inner library classes.
 * Lazily looks up sqlObject from [FlowManager] to get around initialization on top-level
 * Properties or classes that need an adapter.
 */
internal class LazyDBRepresentable<T : Any>(
    sqlObject: KClass<T>
) : DBRepresentable<T> {
    private val realAdapter by lazy { FlowManager.getDBRepresentable(sqlObject) }
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

    override suspend fun DatabaseWrapper.single(query: Query): T? =
        realAdapter.run { single(query) }

    override suspend fun DatabaseWrapper.list(query: Query): List<T> =
        realAdapter.run { list(query) }
}
