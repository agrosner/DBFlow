@file:Suppress("NOTHING_TO_INLINE")

package com.dbflow5.database

import co.touchlab.stately.isolate.IsolateState
import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.ViewAdapter
import com.dbflow5.annotation.opts.DelicateDBFlowApi
import kotlinx.atomicfu.atomic
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

private data class MutableDatabaseHolder(
    var currentHolder: DatabaseHolder
)

/**
 * Holds the main [DatabaseHolder], which provides lookup for database objects by class type.
 */
object DatabaseObjectLookup {

    private val internalDatabaseHolder = IsolateState { MutableDatabaseHolder(DatabaseHolder()) }
    private val databaseHolder: DatabaseHolder
        get() {
            if (!databaseHolderInitialized) {
                throw IllegalStateException(
                    "The global databaseForTable holder is not initialized. " +
                        "Ensure you call FlowManager.init() before accessing the databaseForTable."
                )
            }
            return internalDatabaseHolder.access { it.currentHolder }
        }

    /**
     * This is set at first "merge" of the holder.
     */
    private var databaseHolderInitialized by atomic(false)

    private val loadedModules = IsolateState { hashSetOf<DatabaseHolderFactory>() }

    /**
     * Loads a generated [DatabaseHolderFactory] by creating the holder on the same thread this is
     * called from. This is required to initialize the library, as we do not run any reflection
     * any longer to check if the holder was generated.
     */
    @JvmStatic
    fun loadHolder(holderFactory: DatabaseHolderFactory) {
        if (loadedModules.access { it.contains(holderFactory) }) {
            return
        }

        // Load the database holder, and add it to the global collection.
        internalDatabaseHolder.access { it.currentHolder += holderFactory.create() }
        databaseHolderInitialized = true

        // Cache the holder for future reference.
        loadedModules.access { it.add(holderFactory) }
    }

    /**
     * The [DBRepresentable] for specific type. If cannot find a [ModelAdapter], then it looks
     * for [ViewAdapter]
     */
    @DelicateDBFlowApi
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun <T : Any> getDBRepresentable(modelClass: KClass<T>): DBRepresentable<T> =
        databaseHolder.getModelAdapterOrNull(modelClass)
            ?: databaseHolder.getViewAdapterOrNull(modelClass)
            ?: throwCannotFindAdapter("SQLObjectAdapter", modelClass)

    /**
     * The [ModelAdapter] for specified class type. If cannot find [ModelAdapter], throws
     * [IllegalArgumentException]
     */
    @DelicateDBFlowApi
    @JvmStatic
    @Throws(IllegalArgumentException::class)
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
    @Throws(IllegalArgumentException::class)
    fun <T : Any> getModelViewAdapter(modelViewClass: KClass<T>): ViewAdapter<T> =
        databaseHolder.getViewAdapterOrNull(modelViewClass)
            ?: throwCannotFindAdapter("ModelViewAdapter", modelViewClass)

    private fun throwCannotFindAdapter(type: String, clazz: KClass<*>): Nothing =
        throw IllegalArgumentException(
            "Cannot find $type for $clazz. " +
                "Ensure the class is annotated with proper annotation."
        )
}
