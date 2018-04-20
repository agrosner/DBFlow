@file:Suppress("NOTHING_TO_INLINE")

package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.Synchronized
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.adapter.ModelViewAdapter
import com.raizlabs.dbflow5.adapter.QueryModelAdapter
import com.raizlabs.dbflow5.adapter.RetrievalAdapter
import com.raizlabs.dbflow5.converter.TypeConverter
import com.raizlabs.dbflow5.database.DBFlowDatabase
import com.raizlabs.dbflow5.migration.Migration
import com.raizlabs.dbflow5.quote
import com.raizlabs.dbflow5.runtime.ModelNotifier
import com.raizlabs.dbflow5.runtime.TableNotifierRegister
import com.raizlabs.dbflow5.structure.*
import kotlin.reflect.KClass

expect object FlowManager : FlowCommonManager

/**
 * Description: The main entry point into the generated database code. It uses reflection to look up
 * and construct the generated database holder class used in defining the structure for all databases
 * used in this application.
 */
abstract class FlowCommonManager {

    internal var config: FlowConfig? = null

    protected var globalDatabaseHolder = GlobalDatabaseHolder()

    protected val loadedModules = hashSetOf<KClass<out DatabaseHolder>>()

    private val DEFAULT_DATABASE_HOLDER_NAME = "GeneratedDatabaseHolder"

    private val DEFAULT_DATABASE_HOLDER_PACKAGE_NAME = "com.raizlabs.dbflow5.config"

    private val DEFAULT_DATABASE_HOLDER_CLASSNAME =
        "$DEFAULT_DATABASE_HOLDER_PACKAGE_NAME.$DEFAULT_DATABASE_HOLDER_NAME"

    protected class GlobalDatabaseHolder : DatabaseHolder() {

        var isInitialized = false
            private set

        fun add(holder: DatabaseHolder) {
            databaseDefinitionMap.putAll(holder.databaseDefinitionMap)
            databaseNameMap.putAll(holder.databaseNameMap)
            typeConverters.putAll(holder.typeConverters)
            databaseClassLookupMap.putAll(holder.databaseClassLookupMap)
            isInitialized = true
        }
    }

    /**
     * Returns the table name for the specific model class
     *
     * @param table The class that implements [Model]
     * @return The table name, which can be different than the [Model] class name
     */
    fun getTableName(table: KClass<*>): String {
        return getModelAdapterOrNull(table)?.tableName
            ?: getModelViewAdapterOrNull(table)?.viewName
            ?: throwCannotFindAdapter("ModelAdapter/ModelViewAdapter", table)
    }

    /**
     * @param databaseName The name of the database. Will throw an exception if the databaseForTable doesn't exist.
     * @param tableName    The name of the table in the DB.
     * @return The associated table class for the specified name.
     */
    fun getTableClassForName(databaseName: String, tableName: String): KClass<*> {
        val databaseDefinition = getDatabase(databaseName)
        return databaseDefinition.getModelClassForName(tableName)
            ?: databaseDefinition.getModelClassForName(tableName.quote())
            ?: throw IllegalArgumentException("The specified table $tableName was not found." +
                " Did you forget to add the @Table annotation and point it to $databaseName?")
    }

    /**
     * @param databaseClass The class of the database. Will throw an exception if the databaseForTable doesn't exist.
     * @param tableName     The name of the table in the DB.
     * @return The associated table class for the specified name.
     */

    fun getTableClassForName(databaseClass: KClass<out DBFlowDatabase>, tableName: String): KClass<*> {
        val databaseDefinition = getDatabase(databaseClass)
        return databaseDefinition.getModelClassForName(tableName)
            ?: databaseDefinition.getModelClassForName(tableName.quote())
            ?: throw IllegalArgumentException("The specified table $tableName was not found." +
                " Did you forget to add the @Table annotation and point it to $databaseClass?")
    }

    /**
     * @param table The table to lookup the database for.
     * @return the corresponding [DBFlowDatabase] for the specified model
     */

    fun getDatabaseForTable(table: KClass<*>): DBFlowDatabase {
        checkDatabaseHolder()
        return globalDatabaseHolder.getDatabaseForTable(table)
            ?: throw InvalidDBConfiguration("Model object: $table is not registered with a Database." +
                " Did you forget an annotation?")
    }

    @Suppress("UNCHECKED_CAST")

    fun <T : DBFlowDatabase> getDatabase(databaseClass: KClass<T>): T {
        checkDatabaseHolder()
        return globalDatabaseHolder.getDatabase(databaseClass) as? T
            ?: throw InvalidDBConfiguration("Database: $databaseClass is not a registered Database. " +
                "Did you forget the @Database annotation?")
    }


    fun getDatabaseName(database: KClass<out DBFlowDatabase>): String = getDatabase(database).databaseName

    /**
     * @param databaseName The name of the database. Will throw an exception if the databaseForTable doesn't exist.
     * @return the [DBFlowDatabase] for the specified database
     */

    fun getDatabase(databaseName: String): DBFlowDatabase {
        checkDatabaseHolder()
        return globalDatabaseHolder.getDatabase(databaseName)
            ?: throw InvalidDBConfiguration("The specified database $databaseName was not found. " +
                "Did you forget the @Database annotation?")
    }

    /**
     * Loading the module Database holder via reflection.
     *
     *
     * It is assumed FlowManager.init() is called by the application that uses the
     * module database. This method should only be called if you need to load databases
     * that are part of a module. Building once will give you the ability to add the class.
     */

    fun initModule(generatedClassName: KClass<out DatabaseHolder>) {
        loadDatabaseHolder(generatedClassName)
    }


    fun getConfig(): FlowConfig = config
        ?: throw IllegalStateException("Configuration is not initialized. " +
            "Please call init(FlowConfig) in your application class.")

    /**
     * @return The database holder, creating if necessary using reflection.
     */

    internal abstract fun loadDatabaseHolder(holderClass: KClass<out DatabaseHolder>)

    /**
     * Resets all databases and associated files.
     */
    @Synchronized

    fun reset() {
        globalDatabaseHolder.databaseClassLookupMap.values.forEach { it.reset() }
        globalDatabaseHolder.reset()
        loadedModules.clear()
    }

    /**
     * Close all DB files and resets [FlowConfig] and the [GlobalDatabaseHolder]. Brings
     * DBFlow back to initial application state.
     */
    @Synchronized

    fun close() {
        globalDatabaseHolder.databaseClassLookupMap.values.forEach { it.close() }
        config = null
        globalDatabaseHolder = GlobalDatabaseHolder()
        loadedModules.clear()
    }

    /**
     * Initializes DBFlow, loading the main application Database holder via reflection one time only.
     * This will trigger all creations, updates, and instantiation for each database defined.
     *
     * @param flowConfig The configuration instance that will help shape how DBFlow gets constructed.
     */

    protected fun initialize(flowConfig: FlowConfig) {
        config = config?.merge(flowConfig) ?: flowConfig

        loadDefaultHolderClass(DEFAULT_DATABASE_HOLDER_CLASSNAME)

        flowConfig.databaseHolders.forEach { loadDatabaseHolder(it) }

        if (flowConfig.openDatabasesOnInit) {
            globalDatabaseHolder.databaseDefinitions.forEach {
                // triggers open, create, migrations.
                it.writableDatabase
            }
        }
    }

    abstract fun loadDefaultHolderClass(className: String)

    /**
     * @param objectClass A class with an associated type converter. May return null if not found.
     * @return The specific [TypeConverter] for the specified class. It defines
     * how the custom datatype is handled going into and out of the DB.
     */

    fun getTypeConverterForClass(objectClass: KClass<*>): TypeConverter<*, *>? {
        checkDatabaseHolder()
        return globalDatabaseHolder.getTypeConverterForClass(objectClass)
    }

    // region Getters

    /**
     * Release reference to context and [FlowConfig]
     */

    @Synchronized
    fun destroy() {
        globalDatabaseHolder.databaseClassLookupMap.values.forEach { it.destroy() }
        config = null
        // Reset the global database holder.
        globalDatabaseHolder = GlobalDatabaseHolder()
        loadedModules.clear()
    }

    /**
     * @param modelClass The class that implements [Model] to find an adapter for.
     * @return The adapter associated with the class. If its not a [ModelAdapter],
     * it checks both the [ModelViewAdapter] and [QueryModelAdapter].
     */

    fun <T : Any> getRetrievalAdapter(modelClass: KClass<T>): RetrievalAdapter<T> {
        var retrievalAdapter: RetrievalAdapter<T>? = getModelAdapterOrNull(modelClass)
        if (retrievalAdapter == null) {
            retrievalAdapter = getModelViewAdapterOrNull(modelClass)
            if (retrievalAdapter == null) {
                retrievalAdapter = getQueryModelAdapterOrNull(modelClass)
            }
        }
        return retrievalAdapter ?: throwCannotFindAdapter("RetrievalAdapter", modelClass)
    }


    /**
     * @param modelClass The class of the table
     * @param [T]   The class that implements [Model]
     * @return The associated model adapter (DAO) that is generated from a [Table] class. Handles
     * interactions with the database. This method is meant for internal usage only.
     * We strongly prefer you use the built-in methods associated with [Model] and [BaseModel].
     */

    fun <T : Any> getModelAdapter(modelClass: KClass<T>): ModelAdapter<T> =
        getModelAdapterOrNull(modelClass) ?: throwCannotFindAdapter("ModelAdapter", modelClass)

    /**
     * Returns the model view adapter for a SQLite VIEW. These are only created with the [com.raizlabs.android.dbflow.annotation.ModelView] annotation.
     *
     * @param modelViewClass The class of the VIEW
     * @param [T]  The class that extends [BaseModelView]
     * @return The model view adapter for the specified model view.
     */
    fun <T : Any> getModelViewAdapter(modelViewClass: KClass<T>): ModelViewAdapter<T> =
        getModelViewAdapterOrNull(modelViewClass)
            ?: throwCannotFindAdapter("ModelViewAdapter", modelViewClass)

    /**
     * Returns the query model adapter for an undefined cursor. These are only created with the [T] annotation.
     *
     * @param queryModelClass The class of the query
     * @param [T]  The class that extends [BaseQueryModel]
     * @return The query model adapter for the specified model cursor.
     */

    fun <T : Any> getQueryModelAdapter(queryModelClass: KClass<T>): QueryModelAdapter<T> =
        getQueryModelAdapterOrNull(queryModelClass)
            ?: throwCannotFindAdapter("QueryModelAdapter", queryModelClass)


    fun getModelNotifierForTable(table: KClass<*>): ModelNotifier =
        getDatabaseForTable(table).getModelNotifier()

    fun newRegisterForTable(table: KClass<*>): TableNotifierRegister =
        getModelNotifierForTable(table).newRegister()

    private fun <T : Any> getModelAdapterOrNull(modelClass: KClass<T>): ModelAdapter<T>? =
        getDatabaseForTable(modelClass).getModelAdapterForTable(modelClass)

    private fun <T : Any> getModelViewAdapterOrNull(modelClass: KClass<T>): ModelViewAdapter<T>? =
        getDatabaseForTable(modelClass).getModelViewAdapterForTable(modelClass)

    private fun <T : Any> getQueryModelAdapterOrNull(modelClass: KClass<T>): QueryModelAdapter<T>? =
        getDatabaseForTable(modelClass).getQueryModelAdapterForQueryClass(modelClass)

    /**
     * @param databaseName The name of the database. Will throw an exception if the databaseForTable doesn't exist.
     * @return The map of migrations for the specified database.
     */

    internal fun getMigrations(databaseName: String): Map<Int, List<Migration>> =
        getDatabase(databaseName).migrations

    /**
     * Checks a standard database helper for integrity using quick_check(1).
     *
     * @param databaseName The name of the database to check. Will thrown an exception if it does not exist.
     * @return true if it's integrity is OK.
     */
    fun isDatabaseIntegrityOk(databaseName: String) = getDatabase(databaseName).openHelper.isDatabaseIntegrityOk

    private fun throwCannotFindAdapter(type: String, clazz: KClass<*>): Nothing =
        throw IllegalArgumentException("Cannot find $type for $clazz. Ensure the class is annotated with proper annotation.")

    private fun checkDatabaseHolder() {
        if (!globalDatabaseHolder.isInitialized) {
            throw IllegalStateException("The global databaseForTable holder is not initialized. " +
                "Ensure you call FlowManager.init() before accessing the databaseForTable.")
        }
    }

    // endregion

    /**
     * Exception thrown when a database holder cannot load the databaseForTable holder
     * for a module.
     */
    class ModuleNotFoundException(detailMessage: String, throwable: Throwable)
        : RuntimeException(detailMessage, throwable)

}

/**
 * Easily get access to its [DBFlowDatabase] directly.
 */
inline fun <reified T : DBFlowDatabase> database(): T = database(T::class)

inline fun <T : DBFlowDatabase> database(kClass: KClass<T>): T = FlowManager.getDatabase(kClass)

/**
 * Easily get access to its [DBFlowDatabase] directly.
 */
inline fun <T : DBFlowDatabase, R> database(kClass: KClass<T>, f: T.() -> R): R = database(kClass).f()

inline fun <reified T : DBFlowDatabase> database(f: T.() -> Unit): T = database(T::class).apply(f)

inline fun <T : Any, R> databaseForTable(kClass: KClass<T>, f: DBFlowDatabase.() -> R): R = databaseForTable(kClass).f()

inline fun <T : Any> databaseForTable(kClass: KClass<T>): DBFlowDatabase = FlowManager.getDatabaseForTable(kClass)

inline fun <reified T : Any> databaseForTable(f: DBFlowDatabase.() -> Unit): DBFlowDatabase = databaseForTable(T::class).apply(f)

/**
 * Easily get access to its [DBFlowDatabase] directly.
 */
inline fun <reified T : Any> databaseForTable(): DBFlowDatabase = databaseForTable(T::class)

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

inline val <T : Any> KClass<T>.retrievalAdapter
    get() = FlowManager.getRetrievalAdapter(this)

/**
 * Easily get its [QueryModelAdapter].
 */
inline fun <reified T : Any> queryModelAdapter() = FlowManager.getQueryModelAdapter(T::class)

inline val <T : Any> KClass<T>.queryModelAdapter
    get() = FlowManager.getQueryModelAdapter(this)

/**
 * Easily get its [ModelViewAdapter]
 */
inline fun <reified T : Any> modelViewAdapter() = FlowManager.getModelViewAdapter(T::class)

inline val <T : Any> KClass<T>.modelViewAdapter
    get() = FlowManager.getModelViewAdapter(this)
