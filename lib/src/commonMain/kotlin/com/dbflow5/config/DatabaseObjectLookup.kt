@file:Suppress("NOTHING_TO_INLINE")

package com.dbflow5.config

import com.dbflow5.adapter.WritableDBRepresentable
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.ViewAdapter
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.opts.DelicateDBFlowApi
import kotlin.reflect.KClass
import kotlin.jvm.JvmStatic

/**
 * Holds the main [DatabaseHolder], which provides lookup for database objects by class type.
 */
object DatabaseObjectLookup {

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
     * Loading the module Database holder via reflection.
     *
     *
     * It is assumed FlowManager.init() is called by the application that uses the
     * module database. This method should only be called if you need to load databases
     * that are part of a module. Building once will give you the ability to add the class.
     */
    @JvmStatic
    fun loadHolder(holderFactory: DatabaseHolderFactory) {
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
     * The [WritableDBRepresentable] for specific type. If cannot find a [ModelAdapter], then it looks
     * for [ViewAdapter]
     */
    @DelicateDBFlowApi
    @JvmStatic
    fun <T : Any> getDBRepresentable(modelClass: KClass<T>): WritableDBRepresentable<T> {
        var retrievalAdapter: WritableDBRepresentable<T>? =
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
