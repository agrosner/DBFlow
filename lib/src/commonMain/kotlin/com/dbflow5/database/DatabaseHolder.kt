package com.dbflow5.database

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.QueryAdapter
import com.dbflow5.adapter.ViewAdapter
import com.dbflow5.converter.TypeConverter
import kotlin.reflect.KClass

fun interface DatabaseHolderFactory {
    fun create(): DatabaseHolder
}

/**
 * Description: The base interface for interacting with all of the database and top-level data that's shared
 * between them.
 */
@Suppress("UNCHECKED_CAST")
class DatabaseHolder(
    val tables: Set<ModelAdapter<*>>,
    val views: Set<ViewAdapter<*>>,
    val queries: Set<QueryAdapter<*>>,
    private val typeConverters: Map<KClass<*>, TypeConverter<*, *>> = mapOf(),
) {

    constructor() : this(
        tables = setOf(),
        views = setOf(),
        queries = setOf(),
        typeConverters = mapOf(),
    )

    private val modelAdapterMap = tables.associateBy { it.type }
    private val modelViewAdapterMap = views.associateBy { it.type }
    private val queryModelAdapterMap = queries.associateBy { it.type }

    fun <T : Any> getModelAdapterOrNull(table: KClass<T>): ModelAdapter<T>? =
        modelAdapterMap[table] as ModelAdapter<T>?

    fun <T : Any> getViewAdapterOrNull(table: KClass<T>): ViewAdapter<T>? =
        modelViewAdapterMap[table] as ViewAdapter<T>?

    fun <T : Any> getQueryAdapterOrNull(query: KClass<T>): QueryAdapter<T>? =
        queryModelAdapterMap[query] as QueryAdapter<T>?

    /**
     * Merges the two holders.
     */
    operator fun plus(holder: DatabaseHolder): DatabaseHolder =
        DatabaseHolder(
            tables = tables + holder.tables,
            views = views + holder.views,
            queries = queries + holder.queries,
            typeConverters = typeConverters + holder.typeConverters,
        )
}
