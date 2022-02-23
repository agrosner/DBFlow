@file:Suppress("NOTHING_TO_INLINE")

package com.dbflow5.config

import android.annotation.SuppressLint
import android.content.Context
import com.dbflow5.adapter2.DBRepresentable
import com.dbflow5.adapter2.ModelAdapter
import com.dbflow5.adapter2.QueryRepresentable
import com.dbflow5.adapter2.ViewAdapter
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.opts.DelicateDBFlowApi
import com.dbflow5.converter.TypeConverter
import com.dbflow5.database.scope.WritableDatabaseScope
import com.dbflow5.structure.InvalidDBConfiguration
import com.dbflow5.structure.Model
import kotlin.reflect.KClass

/**
 * Description: The main entry point into the generated database code. It uses reflection to look up
 * and construct the generated database holder class used in defining the structure for all databases
 * used in this application.
 */
object FlowManager {

    @SuppressLint("StaticFieldLeak")
    internal var config: FlowConfig? = null

    private var databaseHolder: DatabaseHolder = DatabaseHolder()

    /**
     * This is set at first "merge" of the holder.
     */
    private var databaseHolderInitialized: Boolean = false

    private val loadedModules = hashSetOf<DatabaseHolderFactory>()

    /**
     * Will throw an exception if this class is not initialized yet in [.init]
     *
     * @return The shared context.
     */
    @JvmStatic
    val context: Context
        get() = config?.context
            ?: throw IllegalStateException(
                "You must provide a valid FlowConfig instance." +
                    " We recommend calling init() in your application class."
            )

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

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T : GeneratedDatabase> getDatabase(databaseClass: KClass<T>): T {
        checkDatabaseHolder()
        return throw InvalidDBConfiguration(
            "Database: ${databaseClass.simpleName} is not a registered Database. " +
                "Did you forget the @Database annotation?"
        )
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T : GeneratedDatabase> getDatabase(databaseClass: Class<T>): T {
        return getDatabase(databaseClass.kotlin)
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
    @Deprecated(message = "Use FlowConfig instead to add modules.")
    fun initModule(holderFactory: DatabaseHolderFactory) {
        loadDatabaseHolder(holderFactory)
    }

    @JvmStatic
    fun getConfig(): FlowConfig = config
        ?: throw IllegalStateException(
            "Configuration is not initialized. " +
                "Please call init(FlowConfig) in your application class."
        )

    /**
     * @return The database holder, creating if necessary using reflection.
     */
    private fun loadDatabaseHolder(holderFactory: DatabaseHolderFactory) {
        if (loadedModules.contains(holderFactory)) {
            return
        }

        try {
            // Load the database holder, and add it to the global collection.
            databaseHolder += holderFactory.create()
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
        config = null
        databaseHolder = DatabaseHolder()
        databaseHolderInitialized = false
        loadedModules.clear()
    }

    /**
     * Helper method to simplify the [.init]. Use [.init] to provide
     * more customization.
     *
     * @param context - should be application context, but not necessary as we retrieve it anyways.
     */
    @JvmStatic
    @JvmOverloads
    fun init(context: Context, config: FlowConfig.Builder.() -> Unit = {}) {
        init(flowConfig(context, config))
    }

    /**
     * Initializes DBFlow, loading the main application Database holder via reflection one time only.
     * This will trigger all creations, updates, and instantiation for each database defined.
     *
     * @param flowConfig The configuration instance that will help shape how DBFlow gets constructed.
     */
    @JvmStatic
    fun init(flowConfig: FlowConfig) {
        config = (config?.merge(flowConfig) ?: flowConfig)
            .also { it.databaseHolders.forEach(::loadDatabaseHolder) }
    }

    /**
     * @param objectClass A class with an associated type converter. May return null if not found.
     * @return The specific [TypeConverter] for the specified class. It defines
     * how the custom datatype is handled going into and out of the DB.
     */
    @JvmStatic
    fun getTypeConverterForClass(objectClass: KClass<*>): TypeConverter<*, *>? {
        checkDatabaseHolder()
        return databaseHolder.getTypeConverterForClass(objectClass)
    }

    // region Getters

    /**
     * Release reference to context and [FlowConfig]
     */
    @JvmStatic
    @Synchronized
    fun destroy() {
        //databaseHolder.databaseClassLookupMap.values.forEach { it.destroy() }
        config = null
        // Reset the global database holder.
        databaseHolder = DatabaseHolder()
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

    private fun checkDatabaseHolder() {
        if (!databaseHolderInitialized) {
            throw IllegalStateException(
                "The global databaseForTable holder is not initialized. " +
                    "Ensure you call FlowManager.init() before accessing the databaseForTable."
            )
        }
    }

    // endregion

    /**
     * Exception thrown when a database holder cannot load the databaseForTable holder
     * for a module.
     */
    class ModuleNotFoundException(detailMessage: String, throwable: Throwable) :
        RuntimeException(detailMessage, throwable)

}

@Deprecated(
    replaceWith = ReplaceWith(""),
    message = "Use DI to provide DB instance"
)
inline fun <reified DB : GeneratedDatabase> database(fn: WritableDatabaseScope<DB>.() -> Unit = {}): DB =
    FlowManager.getDatabase(DB::class).apply { WritableDatabaseScope(this).fn() }

/**
 * Checks a standard database helper for integrity using quick_check(1).
 *
 * @param databaseName The name of the database to check. Will thrown an exception if it does not exist.
 * @return true if it's integrity is OK.
 */
fun <T : DBFlowDatabase> FlowManager.isDatabaseIntegrityOk(clazz: KClass<T>) =
    getDatabase(clazz).openHelper.isDatabaseIntegrityOk
