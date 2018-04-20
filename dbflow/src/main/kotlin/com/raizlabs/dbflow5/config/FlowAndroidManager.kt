package com.raizlabs.dbflow5.config

import android.annotation.SuppressLint
import android.content.Context
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.adapter.ModelViewAdapter
import com.raizlabs.dbflow5.adapter.QueryModelAdapter
import com.raizlabs.dbflow5.adapter.RetrievalAdapter
import kotlin.reflect.KClass

@SuppressLint("StaticFieldLeak")
actual object FlowManager : FlowCommonManager() {

    private var internalContext: Context? = null

    /**
     * Will throw an exception if this class is not initialized yet in [.init]
     *
     * @return The shared context.
     */
    @JvmStatic
    val context: Context
        get() = internalContext
            ?: throw IllegalStateException("You must provide a valid FlowConfig instance." +
                " We recommend calling init() in your application class.")

    /**
     * Helper method to simplify the [.init]. Use [.init] to provide
     * more customization.
     *
     * @param context - should be application context, but not necessary as we retrieve it anyways.
     */
    @JvmStatic
    fun init(context: Context) {
        internalContext = context.applicationContext
        FlowManager.init(FlowConfig.Builder(context).build())
    }

    @JvmStatic
    fun init(flowConfig: FlowConfig) {
        super.initialize(flowConfig)
    }

    @JvmStatic
    fun getDatabaseName(databaseClass: Class<out DBFlowDatabase>): String = getDatabaseName(databaseClass.kotlin)

    @JvmStatic
    fun getDatabase(databaseClass: Class<out DBFlowDatabase>): DBFlowDatabase = getDatabase(databaseClass.kotlin)

    @JvmStatic
    fun getTableName(table: Class<*>): String = getTableName(table.kotlin)

    @JvmStatic
    fun getTableClassForName(databaseClass: Class<out DBFlowDatabase>, name: String): Class<out Any> = getTableClassForName(databaseClass.kotlin, name).java

    @JvmStatic
    fun getDatabaseForTable(table: Class<*>): DBFlowDatabase = getDatabaseForTable(table.kotlin)

    @JvmStatic
    fun <T : Any> getRetrievalAdapter(modelClass: Class<T>): RetrievalAdapter<T> = getRetrievalAdapter(modelClass.kotlin)

    @JvmStatic
    fun <T : Any> getModelAdapter(modelClass: Class<T>): ModelAdapter<T> = getModelAdapter(modelClass.kotlin)

    @JvmStatic
    fun <T : Any> getModelViewAdapter(modelViewClass: Class<T>): ModelViewAdapter<T> = getModelViewAdapter(modelViewClass.kotlin)

    @JvmStatic
    fun <T : Any> getQueryModelAdapter(queryModelClass: Class<T>): QueryModelAdapter<T> = getQueryModelAdapter(queryModelClass.kotlin)

    override fun loadDatabaseHolder(holderClass: KClass<out DatabaseHolder>) {
        if (loadedModules.contains(holderClass)) {
            return
        }

        try {
            // Load the database holder, and add it to the global collection.
            val dbHolder = holderClass.java.newInstance()
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

    override fun loadDefaultHolderClass(className: String) {
        @Suppress("UNCHECKED_CAST")
        try {
            val defaultHolderClass = Class.forName(className) as Class<out DatabaseHolder>
            loadDatabaseHolder(defaultHolderClass.kotlin)
        } catch (e: ModuleNotFoundException) {
            // Ignore this exception since it means the application does not have its
            // own database. The initialization happens because the application is using
            // a module that has a database.
            FlowLog.log(level = FlowLog.Level.W, message = e.message)
        } catch (e: ClassNotFoundException) {
            // warning if a library uses DBFlow with module support but the app you're using doesn't support it.
            FlowLog.log(level = FlowLog.Level.W, message = "Could not find the default GeneratedDatabaseHolder")
        }
    }
}
