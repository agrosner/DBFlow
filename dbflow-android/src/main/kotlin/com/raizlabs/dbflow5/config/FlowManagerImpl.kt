package com.raizlabs.dbflow5.config

import android.annotation.SuppressLint
import android.content.Context
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.adapter.ModelViewAdapter
import com.raizlabs.dbflow5.adapter.QueryModelAdapter
import com.raizlabs.dbflow5.adapter.RetrievalAdapter
import com.raizlabs.dbflow5.database.DBFlowDatabase
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

    @JvmStatic
    fun loadDatabaseHolder(holderClass: Class<out DatabaseHolder>) = loadDatabaseHolder(holderClass.kotlin)

    override fun loadDatabaseHolder(holderClass: KClass<out DatabaseHolder>) {
        FlowJvmCommonManager.loadDatabaseHolder(holderClass)
    }

    override fun loadDefaultHolderClass(className: String) {
        FlowJvmCommonManager.loadDefaultHolderClass(className)
    }
}
