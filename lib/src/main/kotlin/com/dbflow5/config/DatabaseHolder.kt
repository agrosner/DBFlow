package com.dbflow5.config

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.ModelViewAdapter
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.converter.TypeConverter
import kotlin.reflect.KClass

interface DatabaseHolderFactory {
    fun create(): DatabaseHolder
}

/**
 * Description: The base interface for interacting with all of the database and top-level data that's shared
 * between them.
 */
@Suppress("UNCHECKED_CAST")
class DatabaseHolder(
    val databases: Set<GeneratedDatabase>,
    val tables: Set<ModelAdapter<*>>,
    val views: Set<ModelViewAdapter<*>>,
    val queries: Set<RetrievalAdapter<*>>,
    private val typeConverters: Map<KClass<*>, TypeConverter<*, *>> = mapOf(),
) {

    constructor() : this(
        databases = setOf(),
        tables = setOf(),
        views = setOf(),
        queries = setOf(),
        typeConverters = mapOf(),
    )

    private val modelAdapterMap = tables.associateBy { it.table }
    private val modelViewAdapterMap = views.associateBy { it.table }
    private val queryModelAdapterMap = queries.associateBy { it.table }
    internal val databaseClassLookupMap = databases.associateBy { it.associatedDatabaseClassFile }

    /**
     * @param clazz The model value class to get a [TypeConverter]
     * @return Type converter for the specified model value.
     */
    fun getTypeConverterForClass(clazz: KClass<*>): TypeConverter<*, *>? =
        typeConverters[clazz]

    fun getDatabase(databaseClass: KClass<*>): GeneratedDatabase? =
        databaseClassLookupMap[databaseClass]

    internal fun <T : Any> getModelAdapterByTableName(name: String): ModelAdapter<T> =
        modelAdapterMap.values.first { it.name == name } as ModelAdapter<T>

    fun <T : Any> getModelAdapterOrNull(table: KClass<T>): ModelAdapter<T>? =
        modelAdapterMap[table] as ModelAdapter<T>?

    fun <T : Any> getViewAdapterOrNull(table: KClass<T>): ModelViewAdapter<T>? =
        modelViewAdapterMap[table] as ModelViewAdapter<T>?

    fun <T : Any> getQueryAdapterOrNull(query: KClass<T>): RetrievalAdapter<T>? =
        queryModelAdapterMap[query] as RetrievalAdapter<T>?

    /**
     * Merges the two holders.
     */
    operator fun plus(holder: DatabaseHolder): DatabaseHolder =
        DatabaseHolder(
            databases = databases + holder.databases,
            tables = tables + holder.tables,
            views = views + holder.views,
            queries = queries + holder.queries,
            typeConverters = typeConverters + holder.typeConverters,
        )
}
