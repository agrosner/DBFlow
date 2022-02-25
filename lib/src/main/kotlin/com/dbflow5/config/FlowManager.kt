@file:Suppress("NOTHING_TO_INLINE")

package com.dbflow5.config

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.QueryRepresentable
import com.dbflow5.adapter.ViewAdapter
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.opts.DelicateDBFlowApi
import com.dbflow5.structure.Model
import kotlin.reflect.KClass

/**
 * Description: The main entry point into the generated database code. It uses reflection to look up
 * and construct the generated database holder class used in defining the structure for all databases
 * used in this application.
 */
object FlowManager {

    private var internalDatabaseHolder: DatabaseHolder = DatabaseHolder()
    private val databaseHolder: DatabaseHolder
        get() {
            if (!databaseHolderInitialized) {
                throw IllegalStateException(
                    "The global databaseForTable holder is not initialized. " +
                        "Ensure you call FlowManager.init() before accessing the databaseForTable."
                )
            }
            return internalDatabaseHolder
        }

    /**
     * This is set at first "merge" of the holder.
     */
    private var databaseHolderInitialized: Boolean = false

    private val loadedModules = hashSetOf<DatabaseHolderFactory>()

    /**
     * Returns the table name for the specific model class
     *
     * @param table The class that implements [Model]
     * @return The table name, which can be different than the [Model] class name
     */
    @JvmStatic
    fun getTableName(table: KClass<*>): String {
        return databaseHolder.getModelAdapterOrNull(table)?.name
            ?: databaseHolder.getViewAdapterOrNull(table)?.name
            ?: throwCannotFindAdapter("ModelAdapter/ModelViewAdapter/VirtualAdapter", table)
    }

    /**
     * Loading the module Database holder via reflection.
     *
     *
     * It is assumed FlowManager.init() is called by the application that uses the
     * module database. This method should only be called if you need to load databases
     * that are part of a module. Building once will give you the ability to add the class.
     */
    @JvmStatic
    fun init(holderFactory: DatabaseHolderFactory) {
        loadDatabaseHolder(holderFactory)
    }

    /**
     * @return The database holder, creating if necessary using reflection.
     */
    private fun loadDatabaseHolder(holderFactory: DatabaseHolderFactory) {
        if (loadedModules.contains(holderFactory)) {
            return
        }

        try {
            // Load the database holder, and add it to the global collection.
            internalDatabaseHolder += holderFactory.create()
            databaseHolderInitialized = true

            // Cache the holder for future reference.
            loadedModules.add(holderFactory)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw ModuleNotFoundException("Cannot load $holderFactory", e)
        }

    }

    /**
     * Close all DB files and resets [FlowConfig] and the [GlobalDatabaseHolder]. Brings
     * DBFlow back to initial application state.
     */
    @Synchronized
    @JvmStatic
    fun close() {
        internalDatabaseHolder = DatabaseHolder()
        databaseHolderInitialized = false
        loadedModules.clear()
    }

    /**
     * Release reference to context and [FlowConfig]
     */
    @JvmStatic
    @Synchronized
    fun destroy() {
        // Reset the global database holder.
        internalDatabaseHolder = DatabaseHolder()
        databaseHolderInitialized = false
        loadedModules.clear()
    }

    /**
     * @param modelClass The class that contains the [Table] annotation to find an adapter for.
     * @return The adapter associated with the class. If its not a [ModelAdapter],
     * it checks both the [ModelViewAdapter] and [RetrievalAdapter].
     */
    @JvmStatic
    fun <T : Any> getQueryRepresentable(modelClass: KClass<T>): QueryRepresentable<T> {
        var retrievalAdapter: QueryRepresentable<T>? =
            databaseHolder.getModelAdapterOrNull(modelClass)
        if (retrievalAdapter == null) {
            retrievalAdapter = databaseHolder.getViewAdapterOrNull(modelClass)
                ?: databaseHolder.getQueryAdapterOrNull(modelClass)
        }
        return retrievalAdapter ?: throwCannotFindAdapter("RetrievalAdapter", modelClass)
    }

    /**
     * The [DBRepresentable] for specific type. If cannot find a [ModelAdapter], then it looks
     * for [ViewAdapter]
     */
    @DelicateDBFlowApi
    @JvmStatic
    fun <T : Any> getDBRepresentable(modelClass: KClass<T>): DBRepresentable<T> {
        var retrievalAdapter: DBRepresentable<T>? =
            databaseHolder.getModelAdapterOrNull(modelClass)
        if (retrievalAdapter == null) {
            retrievalAdapter = databaseHolder.getViewAdapterOrNull(modelClass)
        }
        return retrievalAdapter ?: throwCannotFindAdapter("SQLObjectAdapter", modelClass)
    }


    /**
     * @param modelClass The class of the table
     * @param [T]   The class with the [Table] annotation.
     *
     * @throws IllegalArgumentException if the adapter does not exist.
     *
     * @return The associated model adapter (DAO) that is generated from a [Table] class. Handles
     * interactions with the database.
     */
    @DelicateDBFlowApi
    @JvmStatic
    fun <T : Any> getModelAdapter(modelClass: KClass<T>): ModelAdapter<T> =
        databaseHolder.getModelAdapterOrNull(modelClass) ?: throwCannotFindAdapter(
            "ModelAdapter",
            modelClass
        )

    @DelicateDBFlowApi
    @JvmStatic
    fun <T : Any> getModelAdapterByTableName(name: String): ModelAdapter<T> =
        databaseHolder.getModelAdapterByTableName(name)

    /**
     * Returns the model view adapter for a SQLite VIEW. These are only created with the [com.dbflow5.annotation.ModelView] annotation.
     *
     * @throws IllegalArgumentException if the adapter does not exist.
     *
     * @param modelViewClass The class of the VIEW
     * @param [T]  The class that has a [com.dbflow5.annotation.ModelView] annotation.
     * @return The model view adapter for the specified class.
     */
    @DelicateDBFlowApi
    @JvmStatic
    fun <T : Any> getModelViewAdapter(modelViewClass: KClass<T>): ViewAdapter<T> =
        databaseHolder.getViewAdapterOrNull(modelViewClass)
            ?: throwCannotFindAdapter("ModelViewAdapter", modelViewClass)

    private fun throwCannotFindAdapter(type: String, clazz: KClass<*>): Nothing =
        throw IllegalArgumentException(
            "Cannot find $type for $clazz. " +
                "Ensure the class is annotated with proper annotation."
        )

    /**
     * Exception thrown when a database holder cannot load the databaseForTable holder
     * for a module.
     */
    class ModuleNotFoundException(detailMessage: String, throwable: Throwable) :
        RuntimeException(detailMessage, throwable)

}
