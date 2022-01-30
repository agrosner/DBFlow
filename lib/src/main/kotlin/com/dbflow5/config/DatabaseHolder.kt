package com.dbflow5.config

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.ModelViewAdapter
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.converter.TypeConverter
import kotlin.reflect.KClass

interface MutableHolder {
    fun putDatabase(databaseDefinition: DBFlowDatabase)
    fun putModelAdapter(modelAdapter: ModelAdapter<*>)
    fun putQueryAdapter(retrievalAdapter: RetrievalAdapter<*>)
    fun putViewAdapter(modelViewAdapter: ModelViewAdapter<*>)
}

/**
 * Description: The base interface for interacting with all of the database and top-level data that's shared
 * between them.
 */
abstract class DatabaseHolder : MutableHolder {

    val modelAdapterMap = hashMapOf<KClass<*>, ModelAdapter<*>>()
    val modelViewAdapterMap = linkedMapOf<KClass<*>, ModelViewAdapter<*>>()
    val queryModelAdapterMap = linkedMapOf<KClass<*>, RetrievalAdapter<*>>()

    val databaseNameMap: MutableMap<String, DBFlowDatabase> = hashMapOf()
    val databaseClassLookupMap: MutableMap<KClass<*>, DBFlowDatabase> = hashMapOf()

    @JvmField
    val typeConverters: MutableMap<KClass<*>, TypeConverter<*, *>> = hashMapOf()

    val databaseDefinitions: List<DBFlowDatabase>
        get() = databaseNameMap.values.toList()

    /**
     * @param clazz The model value class to get a [TypeConverter]
     * @return Type converter for the specified model value.
     */
    fun getTypeConverterForClass(clazz: KClass<*>): TypeConverter<*, *>? =
        typeConverters[clazz]

    fun getDatabase(databaseClass: KClass<*>): DBFlowDatabase? =
        databaseClassLookupMap[databaseClass]

    /**
     * @param databaseName The name of the database to retrieve
     * @return The database that has the specified name
     */
    fun getDatabase(databaseName: String): DBFlowDatabase? = databaseNameMap[databaseName]

    fun <T : Any> getModelAdapterOrNull(table: KClass<T>): ModelAdapter<T>? =
        modelAdapterMap[table] as ModelAdapter<T>?

    fun <T : Any> getViewAdapterOrNull(table: KClass<T>): ModelViewAdapter<T>? =
        modelViewAdapterMap[table] as ModelViewAdapter<T>?

    fun <T : Any> getQueryAdapterOrNull(query: KClass<T>): RetrievalAdapter<T>? =
        queryModelAdapterMap[query] as RetrievalAdapter<T>?

    override fun putDatabase(databaseDefinition: DBFlowDatabase) {
        databaseNameMap[databaseDefinition.databaseName] = databaseDefinition
        databaseClassLookupMap[databaseDefinition.associatedDatabaseClassFile] =
            databaseDefinition
    }

    override fun putModelAdapter(modelAdapter: ModelAdapter<*>) {
        modelAdapterMap[modelAdapter.table] = modelAdapter
    }

    override fun putQueryAdapter(retrievalAdapter: RetrievalAdapter<*>) {
        queryModelAdapterMap[retrievalAdapter.table] = retrievalAdapter
    }

    override fun putViewAdapter(modelViewAdapter: ModelViewAdapter<*>) {
        modelViewAdapterMap[modelViewAdapter.table] = modelViewAdapter
    }

    fun putTypeConverter(type: KClass<*>, typeConverter: TypeConverter<*, *>) {
        typeConverters[type] = typeConverter
    }

    fun reset() {
        databaseNameMap.clear()
        databaseClassLookupMap.clear()
        typeConverters.clear()
        modelViewAdapterMap.clear()
        modelAdapterMap.clear()
        queryModelAdapterMap.clear()
    }
}
