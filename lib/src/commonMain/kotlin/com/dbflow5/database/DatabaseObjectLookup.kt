@file:Suppress("NOTHING_TO_INLINE")

package com.dbflow5.database

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.QueryAdapter
import com.dbflow5.adapter.QueryRepresentable
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

    private val internalDatabaseHolder = MutableDatabaseHolder(DatabaseHolder())
    private val databaseHolder: DatabaseHolder
        get() {
            if (!databaseHolderInitialized) {
                throw IllegalStateException(
                    "The global DatabaseHolder is not initialized. " +
                        "Open a database with createDB or call DatabaseObjectLookup.registerAdapters() " +
                        "before dynamically accessing DB representable types."
                )
            }
            return internalDatabaseHolder.currentHolder
        }

    /**
     * This is set at first "merge" of the holder.
     */
    private var databaseHolderInitialized by atomic(false)

    private val loadedModules = hashSetOf<DatabaseHolderFactory>()

    /**
     * Loads a generated [DatabaseHolderFactory] by creating the holder on the same thread this is
     * called from. This is required to initialize the library, as we do not run any reflection
     * any longer to check if the holder was generated.
     */
    @JvmStatic
    fun loadHolder(holderFactory: DatabaseHolderFactory) {
        if (loadedModules.contains(holderFactory)) {
            return
        }

        // Load the database holder, and add it to the global collection.
        internalDatabaseHolder.currentHolder += holderFactory.create()
        databaseHolderInitialized = true

        // Cache the holder for future reference.
        loadedModules.add(holderFactory)
    }

    /**
     * Registers generated adapters so KClass lookups work without a holder factory object.
     *
     * Re-registering a type replaces the previous adapter. `create()` runs once per
     * database instance, and lookups must return the instances belonging to the
     * newest instance so [DBFlowDatabase.tableObserver] and query-site adapters agree.
     */
    @JvmStatic
    fun registerAdapters(vararg adapters: QueryRepresentable<*>) {
        val tables = adapters.filterIsInstance<ModelAdapter<*>>().toSet()
        val views = adapters.filterIsInstance<ViewAdapter<*>>().toSet()
        val queries = adapters.filterIsInstance<QueryAdapter<*>>().toSet()
        if (tables.isEmpty() && views.isEmpty() && queries.isEmpty()) return
        internalDatabaseHolder.currentHolder += DatabaseHolder(
            tables = tables,
            views = views,
            queries = queries,
        )
        databaseHolderInitialized = true
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
     * Returns the [ModelAdapter] for specified class type. If cannot find [ModelAdapter], throws
     * [IllegalArgumentException].
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
     * Returns the [ViewAdapter] for the specified class type. If cannot find a [ViewAdapter],
     * throws [IllegalArgumentException].
     */
    @DelicateDBFlowApi
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun <T : Any> getViewAdapter(modelViewClass: KClass<T>): ViewAdapter<T> =
        databaseHolder.getViewAdapterOrNull(modelViewClass)
            ?: throwCannotFindAdapter("ViewAdapter", modelViewClass)

    private fun throwCannotFindAdapter(type: String, clazz: KClass<*>): Nothing =
        throw IllegalArgumentException(
            "Cannot find $type for $clazz. " +
                "Ensure the class is annotated with proper annotation."
        )
}
