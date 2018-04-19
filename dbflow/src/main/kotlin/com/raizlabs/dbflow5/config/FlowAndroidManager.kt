package com.raizlabs.dbflow5.config

import android.annotation.SuppressLint
import android.content.Context
import kotlin.reflect.KClass

@SuppressLint("StaticFieldLeak")
actual object FlowManager : FlowCommonManager() {

    private var internalContext: Context? = null

    /**
     * Will throw an exception if this class is not initialized yet in [.init]
     *
     * @return The shared context.
     */
    @kotlin.jvm.JvmStatic
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
    @kotlin.jvm.JvmStatic
    fun init(context: Context) {
        internalContext = context.applicationContext
        FlowManager.init(FlowConfig.Builder(context).build())
    }

    @JvmStatic
    fun init(flowConfig: FlowConfig) {
        super.initialize(flowConfig)
    }

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
