@file:Suppress("NOTHING_TO_INLINE")

package com.dbflow5.config

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.ModelViewAdapter
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.annotation.Table
import com.dbflow5.converter.TypeConverter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.migration.Migration
import com.dbflow5.quote
import com.dbflow5.runtime.ModelNotifier
import com.dbflow5.runtime.TableNotifierRegister
import com.dbflow5.structure.BaseModel
import com.dbflow5.structure.BaseModelView
import com.dbflow5.structure.BaseQueryModel
import com.dbflow5.structure.InvalidDBConfiguration
import com.dbflow5.structure.Model
import org.jetbrains.annotations.TestOnly
import kotlin.reflect.KClass

/**
 * Description: The main entry point into the generated database code. It uses reflection to look up
 * and construct the generated database holder class used in defining the structure for all databases
 * used in this application.
 */
object FlowManager {

    @SuppressLint("StaticFieldLeak")
    internal var config: FlowConfig? = null

    private var globalDatabaseHolder = GlobalDatabaseHolder()

    private val loadedModules = hashSetOf<Class<out DatabaseHolder>>()

    private val DEFAULT_DATABASE_HOLDER_NAME = "GeneratedDatabaseHolder"

    private val DEFAULT_DATABASE_HOLDER_PACKAGE_NAME = FlowManager::class.java.`package`.name

    private val DEFAULT_DATABASE_HOLDER_CLASSNAME =
        "$DEFAULT_DATABASE_HOLDER_PACKAGE_NAME.$DEFAULT_DATABASE_HOLDER_NAME"

    /**
     * Override for testing
     */
    @set:TestOnly
    var globalContentResolver: ContentResolver? = null

    /**
     * Will throw an exception if this class is not initialized yet in [.init]
     *
     * @return The shared context.
     */
    @JvmStatic
    val context: Context
        get() = config?.context
            ?: throw IllegalStateException("You must provide a valid FlowConfig instance." +
                " We recommend calling init() in your application class.")

    val contentResolver: ContentResolver
        get() = globalContentResolver ?: context.contentResolver


    private class GlobalDatabaseHolder : DatabaseHolder() {

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
    @JvmStatic
    fun getTableName(table: Class<*>): String {
        return getModelAdapterOrNull(table)?.tableName
            ?: getModelViewAdapterOrNull(table)?.viewName
            ?: throwCannotFindAdapter("ModelAdapter/ModelViewAdapter/VirtualAdapter", table)
    }

    /**
     * @param databaseName The name of the database. Will throw an exception if the databaseForTable doesn't exist.
     * @param tableName    The name of the table in the DB.
     * @return The associated table class for the specified name.
     */
    @JvmStatic
    fun getTableClassForName(databaseName: String, tableName: String): Class<*> {
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
    @JvmStatic
    fun getTableClassForName(databaseClass: Class<out DBFlowDatabase>, tableName: String): Class<*> {
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
    @JvmStatic
    fun getDatabaseForTable(table: Class<*>): DBFlowDatabase {
        checkDatabaseHolder()
        return globalDatabaseHolder.getDatabaseForTable(table)
            ?: throw InvalidDBConfiguration("Model object: ${table.name} is not registered with a Database." +
                " Did you forget an annotation?")
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T : DBFlowDatabase> getDatabase(databaseClass: Class<T>): T {
        checkDatabaseHolder()
        return globalDatabaseHolder.getDatabase(databaseClass) as? T
            ?: throw InvalidDBConfiguration("Database: ${databaseClass.name} is not a registered Database. " +
                "Did you forget the @Database annotation?")
    }

    @JvmStatic
    fun getDatabaseName(database: Class<out DBFlowDatabase>): String = getDatabase(database).databaseName

    /**
     * @param databaseName The name of the database. Will throw an exception if the databaseForTable doesn't exist.
     * @return the [DBFlowDatabase] for the specified database
     */
    @JvmStatic
    fun getDatabase(databaseName: String): DBFlowDatabase {
        checkDatabaseHolder()
        return globalDatabaseHolder.getDatabase(databaseName)
            ?: throw InvalidDBConfiguration("The specified database $databaseName was not found. " +
                "Did you forget the @Database annotation?")
    }

    @Deprecated(replaceWith = ReplaceWith("FlowManager.getDatabaseForTable(table)"),
        message = "This method is no longer needed. DBFlowDatabase now delegates to the DatabaseWrapper.")
    @JvmStatic
    fun getWritableDatabaseForTable(table: Class<*>): DatabaseWrapper =
        getDatabaseForTable(table).writableDatabase

    @Deprecated(replaceWith = ReplaceWith("FlowManager.getDatabase(databaseName)"),
        message = "This method is no longer needed. DBFlowDatabase now delegates to the DatabaseWrapper.")
    @JvmStatic
    fun getWritableDatabase(databaseName: String): DatabaseWrapper =
        getDatabase(databaseName).writableDatabase

    @Deprecated(replaceWith = ReplaceWith("FlowManager.getDatabase(databaseClass)"),
        message = "This method is no longer needed. DBFlowDatabase now delegates to the DatabaseWrapper.")
    @JvmStatic
    fun getWritableDatabase(databaseClass: Class<out DBFlowDatabase>): DatabaseWrapper =
        getDatabase(databaseClass).writableDatabase

    /**
     * Loading the module Database holder via reflection.
     *
     *
     * It is assumed FlowManager.init() is called by the application that uses the
     * module database. This method should only be called if you need to load databases
     * that are part of a module. Building once will give you the ability to add the class.
     */
    @JvmStatic
    fun initModule(generatedClassName: Class<out DatabaseHolder>) {
        loadDatabaseHolder(generatedClassName)
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
        loadDatabaseHolder(generatedClassName.java)
    }

    @JvmStatic
    fun getConfig(): FlowConfig = config
        ?: throw IllegalStateException("Configuration is not initialized. " +
            "Please call init(FlowConfig) in your application class.")

    /**
     * @return The database holder, creating if necessary using reflection.
     */
    private fun loadDatabaseHolder(holderClass: Class<out DatabaseHolder>) {
        if (loadedModules.contains(holderClass)) {
            return
        }

        try {
            // Load the database holder, and add it to the global collection.
            val dbHolder = holderClass.newInstance()
            if (dbHolder != null) {
                globalDatabaseHolder.add(dbHolder)

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
        globalDatabaseHolder.databaseClassLookupMap.values.forEach { it.reset() }
        globalDatabaseHolder.reset()
        loadedModules.clear()
    }

    /**
     * Close all DB files and resets [FlowConfig] and the [GlobalDatabaseHolder]. Brings
     * DBFlow back to initial application state.
     */
    @Synchronized
    @JvmStatic
    fun close() {
        globalDatabaseHolder.databaseClassLookupMap.values.forEach { it.close() }
        config = null
        globalDatabaseHolder = GlobalDatabaseHolder()
        loadedModules.clear()
    }

    /**
     * Helper method to simplify the [.init]. Use [.init] to provide
     * more customization.
     *
     * @param context - should be application context, but not necessary as we retrieve it anyways.
     */
    @JvmStatic
    fun init(context: Context) {
        init(FlowConfig.Builder(context).build())
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
            val defaultHolderClass = Class.forName(DEFAULT_DATABASE_HOLDER_CLASSNAME) as Class<out DatabaseHolder>
            loadDatabaseHolder(defaultHolderClass)
        } catch (e: ModuleNotFoundException) {
            // Ignore this exception since it means the application does not have its
            // own database. The initialization happens because the application is using
            // a module that has a database.
            FlowLog.log(level = FlowLog.Level.W, message = e.message)
        } catch (e: ClassNotFoundException) {
            // warning if a library uses DBFlow with module support but the app you're using doesn't support it.
            FlowLog.log(level = FlowLog.Level.W, message = "Could not find the default GeneratedDatabaseHolder")
        }

        flowConfig.databaseHolders.forEach { loadDatabaseHolder(it) }

        if (flowConfig.openDatabasesOnInit) {
            globalDatabaseHolder.databaseDefinitions.forEach {
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
    fun getTypeConverterForClass(objectClass: Class<*>): TypeConverter<*, *>? {
        checkDatabaseHolder()
        return globalDatabaseHolder.getTypeConverterForClass(objectClass)
    }

    // region Getters

    /**
     * Release reference to context and [FlowConfig]
     */
    @JvmStatic
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
     * it checks both the [ModelViewAdapter] and [RetrievalAdapter].
     */
    @JvmStatic
    fun <T : Any> getRetrievalAdapter(modelClass: Class<T>): RetrievalAdapter<T> {
        var retrievalAdapter: RetrievalAdapter<T>? = getModelAdapterOrNull(modelClass)
        if (retrievalAdapter == null) {
            retrievalAdapter = getModelViewAdapterOrNull(modelClass)
                ?: getQueryModelAdapterOrNull(modelClass)
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
    @JvmStatic
    fun <T : Any> getModelAdapter(modelClass: Class<T>): ModelAdapter<T> =
        getModelAdapterOrNull(modelClass) ?: throwCannotFindAdapter("ModelAdapter", modelClass)

    /**
     * Returns the model view adapter for a SQLite VIEW. These are only created with the [com.dbflow5.annotation.ModelView] annotation.
     *
     * @param modelViewClass The class of the VIEW
     * @param [T]  The class that extends [BaseModelView]
     * @return The model view adapter for the specified model view.
     */
    @JvmStatic
    fun <T : Any> getModelViewAdapter(modelViewClass: Class<T>): ModelViewAdapter<T> =
        getModelViewAdapterOrNull(modelViewClass)
            ?: throwCannotFindAdapter("ModelViewAdapter", modelViewClass)

    /**
     * Returns the query model adapter for the model class. These are only created with the [T] annotation.
     *
     * @param queryModelClass The class of the query
     * @param [T]  The class that extends [BaseQueryModel]
     * @return The query model adapter for the specified model cursor.
     */
    @JvmStatic
    fun <T : Any> getQueryModelAdapter(queryModelClass: Class<T>): RetrievalAdapter<T> =
        getQueryModelAdapterOrNull(queryModelClass)
            ?: throwCannotFindAdapter("RetrievalAdapter", queryModelClass)

    @JvmStatic
    fun getModelNotifierForTable(table: Class<*>): ModelNotifier =
        getDatabaseForTable(table).getModelNotifier()

    @JvmStatic
    fun newRegisterForTable(table: Class<*>): TableNotifierRegister =
        getModelNotifierForTable(table).newRegister()

    private fun <T : Any> getModelAdapterOrNull(modelClass: Class<T>): ModelAdapter<T>? =
        getDatabaseForTable(modelClass).getModelAdapterForTable(modelClass)

    private fun <T : Any> getModelViewAdapterOrNull(modelClass: Class<T>): ModelViewAdapter<T>? =
        getDatabaseForTable(modelClass).getModelViewAdapterForTable(modelClass)

    private fun <T : Any> getQueryModelAdapterOrNull(modelClass: Class<T>): RetrievalAdapter<T>? =
        getDatabaseForTable(modelClass).getQueryModelAdapterForQueryClass(modelClass)

    /**
     * @param databaseName The name of the database. Will throw an exception if the databaseForTable doesn't exist.
     * @return The map of migrations for the specified database.
     */
    @JvmStatic
    internal fun getMigrations(databaseName: String): Map<Int, List<Migration>> =
        getDatabase(databaseName).migrations

    /**
     * Checks a standard database helper for integrity using quick_check(1).
     *
     * @param databaseName The name of the database to check. Will thrown an exception if it does not exist.
     * @return true if it's integrity is OK.
     */
    @JvmStatic
    fun isDatabaseIntegrityOk(databaseName: String) = getDatabase(databaseName).openHelper.isDatabaseIntegrityOk

    private fun throwCannotFindAdapter(type: String, clazz: Class<*>): Nothing =
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
inline fun <T : DBFlowDatabase> database(kClass: KClass<T>, f: (db: T) -> Unit = {}): T =
    FlowManager.getDatabase(kClass.java).apply(f)

/**
 * Easily get access to its [DBFlowDatabase] directly.
 */
inline fun <reified T : DBFlowDatabase> database(f: (db: T) -> Unit = {}): T =
    FlowManager.getDatabase(T::class.java).apply(f)

/**
 * Easily get access to its [DBFlowDatabase] directly.
 */
inline fun <reified T : Any> databaseForTable(f: (db: DBFlowDatabase) -> Unit = {}): DBFlowDatabase =
    FlowManager.getDatabaseForTable(T::class.java).apply(f)

/**
 * Easily get access to its [DBFlowDatabase] directly.
 */
inline fun <T : Any> databaseForTable(clazz: KClass<T>, f: (db: DBFlowDatabase) -> Unit = {}): DBFlowDatabase =
    FlowManager.getDatabaseForTable(clazz.java).apply(f)

/**
 * Easily get its table name.
 */
inline fun <reified T : Any> tableName(): String = FlowManager.getTableName(T::class.java)

/**
 * Easily get its [ModelAdapter].
 */
inline fun <reified T : Any> modelAdapter() = FlowManager.getModelAdapter(T::class.java)

inline val <T : Any> KClass<T>.modelAdapter
    get() = FlowManager.getModelAdapter(this.java)


inline val <T : Any> Class<T>.modelAdapter
    get() = FlowManager.getModelAdapter(this)

@Deprecated(message = "Use retrievalAdapter().", replaceWith = ReplaceWith("retrievalAdapter()", "com.dbflow5.config"))
inline fun <reified T : Any> queryModelAdapter() = FlowManager.getQueryModelAdapter(T::class.java)

@Deprecated(message = "Use retrievalAdapter.", replaceWith = ReplaceWith("retrievalAdapter", "com.dbflow5.config"))
inline val <T : Any> KClass<T>.queryModelAdapter
    get() = FlowManager.getQueryModelAdapter(this.java)

@Deprecated(message = "Use retrievalAdapter().", replaceWith = ReplaceWith("retrievalAdapter", "com.dbflow5.config"))
inline val <T : Any> Class<T>.queryModelAdapter
    get() = FlowManager.getQueryModelAdapter(this)

/**
 * Easily get its [RetrievalAdapter].
 */
inline fun <reified T : Any> retrievalAdapter() = FlowManager.getQueryModelAdapter(T::class.java)

inline val <T : Any> KClass<T>.retrievalAdapter
    get() = FlowManager.getQueryModelAdapter(this.java)

inline val <T : Any> Class<T>.retrievalAdapter
    get() = FlowManager.getQueryModelAdapter(this)

/**
 * Easily get its [ModelViewAdapter]
 */
inline fun <reified T : Any> modelViewAdapter() = FlowManager.getModelViewAdapter(T::class.java)

inline val <T : Any> KClass<T>.modelViewAdapter
    get() = FlowManager.getModelViewAdapter(this.java)

inline val <T : Any> Class<T>.modelViewAdapter
    get() = FlowManager.getModelViewAdapter(this)