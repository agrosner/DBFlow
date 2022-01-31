@file:Suppress("NOTHING_TO_INLINE")

package com.dbflow5.config

import android.annotation.SuppressLint
import android.content.Context
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.ModelViewAdapter
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.annotation.Table
import com.dbflow5.converter.TypeConverter
import com.dbflow5.database.scope.DatabaseScope
import com.dbflow5.database.scope.DatabaseScopeImpl
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

    private var databaseHolder = GlobalDatabaseHolder()

    private val loadedModules = hashSetOf<KClass<out DatabaseHolder>>()

    private val DEFAULT_DATABASE_HOLDER_NAME = "GeneratedDatabaseHolder"

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private val DEFAULT_DATABASE_HOLDER_PACKAGE_NAME = FlowManager::class.java.`package`.name

    private val DEFAULT_DATABASE_HOLDER_CLASSNAME =
        "$DEFAULT_DATABASE_HOLDER_PACKAGE_NAME.$DEFAULT_DATABASE_HOLDER_NAME"

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

    private class GlobalDatabaseHolder : DatabaseHolder() {

        var isInitialized = false
            private set

        fun add(holder: DatabaseHolder) {
            databaseNameMap.putAll(holder.databaseNameMap)
            typeConverters.putAll(holder.typeConverters)
            databaseClassLookupMap.putAll(holder.databaseClassLookupMap)
            modelAdapterMap.putAll(holder.modelAdapterMap)
            modelViewAdapterMap.putAll(holder.modelViewAdapterMap)
            queryModelAdapterMap.putAll(holder.queryModelAdapterMap)
            isInitialized = true
        }
    }

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
    fun <T : DBFlowDatabase> getDatabase(databaseClass: KClass<T>): T {
        checkDatabaseHolder()
        return databaseHolder.getDatabase(databaseClass) as? T
            ?: throw InvalidDBConfiguration(
                "Database: ${databaseClass.simpleName} is not a registered Database. " +
                    "Did you forget the @Database annotation?"
            )
    }

    /**
     * @param databaseName The name of the database. Will throw an exception if the databaseForTable doesn't exist.
     * @return the [DBFlowDatabase] for the specified database
     */
    @JvmStatic
    fun getDatabase(databaseName: String): DBFlowDatabase {
        checkDatabaseHolder()
        return databaseHolder.getDatabase(databaseName)
            ?: throw InvalidDBConfiguration(
                "The specified database $databaseName was not found. " +
                    "Did you forget the @Database annotation?"
            )
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
    fun initModule(generatedClassName: KClass<out DatabaseHolder>) {
        loadDatabaseHolder(generatedClassName)
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
    private fun loadDatabaseHolder(holderClass: KClass<out DatabaseHolder>) {
        if (loadedModules.contains(holderClass)) {
            return
        }

        try {
            // Load the database holder, and add it to the global collection.
            val dbHolder: DatabaseHolder? = holderClass.java.newInstance()
            if (dbHolder != null) {
                databaseHolder.add(dbHolder)

                // Cache the holder for future reference.
                loadedModules.add(holderClass)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            throw ModuleNotFoundException("Cannot load $holderClass", e)
        }

    }

    /**
     * Resets all databases and associated files.
     */
    @Synchronized
    @JvmStatic
    fun reset() {
        databaseHolder.databaseClassLookupMap.values.forEach { it.reset() }
        databaseHolder.reset()
        loadedModules.clear()
    }

    /**
     * Close all DB files and resets [FlowConfig] and the [GlobalDatabaseHolder]. Brings
     * DBFlow back to initial application state.
     */
    @Synchronized
    @JvmStatic
    fun close() {
        databaseHolder.databaseClassLookupMap.values.forEach { it.close() }
        config = null
        databaseHolder = GlobalDatabaseHolder()
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
        config = config?.merge(flowConfig) ?: flowConfig

        @Suppress("UNCHECKED_CAST")
        try {
            val defaultHolderClass =
                Class.forName(DEFAULT_DATABASE_HOLDER_CLASSNAME) as Class<out DatabaseHolder>
            loadDatabaseHolder(defaultHolderClass.kotlin)
        } catch (e: ModuleNotFoundException) {
            // Ignore this exception sinc`e it means the application does not have its
            // own database. The initialization happens because the application is using
            // a module that has a database.
            FlowLog.log(level = FlowLog.Level.W, message = e.message)
        } catch (e: ClassNotFoundException) {
            // warning if a library uses DBFlow with module support but the app you're using doesn't support it.
            FlowLog.log(
                level = FlowLog.Level.W,
                message = "Could not find the default GeneratedDatabaseHolder"
            )
        }

        flowConfig.databaseHolders.forEach { loadDatabaseHolder(it) }

        if (flowConfig.openDatabasesOnInit) {
            databaseHolder.databaseDefinitions.forEach {
                // triggers open, create, migrations.
                it.writableDatabase
            }
        }
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
        databaseHolder.databaseClassLookupMap.values.forEach { it.destroy() }
        config = null
        // Reset the global database holder.
        databaseHolder = GlobalDatabaseHolder()
        loadedModules.clear()
    }

    /**
     * @param modelClass The class that contains the [Table] annotation to find an adapter for.
     * @return The adapter associated with the class. If its not a [ModelAdapter],
     * it checks both the [ModelViewAdapter] and [RetrievalAdapter].
     */
    @JvmStatic
    fun <T : Any> getRetrievalAdapter(modelClass: KClass<T>): RetrievalAdapter<T> {
        var retrievalAdapter: RetrievalAdapter<T>? =
            databaseHolder.getModelAdapterOrNull(modelClass)
        if (retrievalAdapter == null) {
            retrievalAdapter = databaseHolder.getViewAdapterOrNull(modelClass)
                ?: databaseHolder.getQueryAdapterOrNull(modelClass)
        }
        return retrievalAdapter ?: throwCannotFindAdapter("RetrievalAdapter", modelClass)
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
    @JvmStatic
    fun <T : Any> getModelViewAdapter(modelViewClass: KClass<T>): ModelViewAdapter<T> =
        databaseHolder.getViewAdapterOrNull(modelViewClass)
            ?: throwCannotFindAdapter("ModelViewAdapter", modelViewClass)

    /**
     * Returns the query model adapter for the model class. These are only created with the [QueryModel] annotation.
     *
     * @throws IllegalArgumentException if the adapter does not exist.
     *
     * @param queryModelClass The class of the query
     * @param [T]  The class that has a [QueryModel] annotation.
     * @return The query model adapter for the specified class.
     */
    @JvmStatic
    fun <T : Any> getQueryModelAdapter(queryModelClass: KClass<T>): RetrievalAdapter<T> =
        databaseHolder.getQueryAdapterOrNull(queryModelClass)
            ?: throwCannotFindAdapter("RetrievalAdapter", queryModelClass)

    /**
     * Checks a standard database helper for integrity using quick_check(1).
     *
     * @param databaseName The name of the database to check. Will thrown an exception if it does not exist.
     * @return true if it's integrity is OK.
     */
    @JvmStatic
    fun isDatabaseIntegrityOk(databaseName: String) =
        getDatabase(databaseName).openHelper.isDatabaseIntegrityOk

    private fun throwCannotFindAdapter(type: String, clazz: KClass<*>): Nothing =
        throw IllegalArgumentException(
            "Cannot find $type for $clazz. " +
                "Ensure the class is annotated with proper annotation."
        )

    private fun checkDatabaseHolder() {
        if (!databaseHolder.isInitialized) {
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

/**
 * Easily get access to its [DBFlowDatabase] directly.
 */
inline fun <reified DB : DBFlowDatabase> database(fn: DatabaseScope<DB>.() -> Unit = {}): DB =
    FlowManager.getDatabase(DB::class).apply {
        DatabaseScopeImpl<DB>(this).fn()
    }

/**
 * Easily get its table name.
 */
inline fun <reified T : Any> tableName(): String = FlowManager.getTableName(T::class)

/**
 * Easily get its [ModelAdapter].
 */
inline fun <reified T : Any> modelAdapter() = FlowManager.getModelAdapter(T::class)

inline val <T : Any> KClass<T>.modelAdapter
    get() = FlowManager.getModelAdapter(this)

inline fun <reified T : Any> queryModelAdapter() = FlowManager.getQueryModelAdapter(T::class)

inline val <T : Any> KClass<T>.queryModelAdapter
    get() = FlowManager.getQueryModelAdapter(this)

/**
 * Easily get its [RetrievalAdapter].
 */
inline fun <reified T : Any> retrievalAdapter() = FlowManager.getRetrievalAdapter(T::class)

inline val <T : Any> KClass<T>.retrievalAdapter
    get() = FlowManager.getRetrievalAdapter(this)

/**
 * Easily get its [ModelViewAdapter]
 */
inline fun <reified T : Any> modelViewAdapter() = FlowManager.getModelViewAdapter(T::class)

inline val <T : Any> KClass<T>.modelViewAdapter
    get() = FlowManager.getModelViewAdapter(this)