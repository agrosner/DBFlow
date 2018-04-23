package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.config.FlowCommonManager.ModuleNotFoundException
import com.raizlabs.dbflow5.config.FlowLog.Level.W
import kotlin.reflect.KClass

object FlowJvmCommonManager {

    fun loadDatabaseHolder(holderClass: KClass<out DatabaseHolder>) {
        if (FlowManager.hasLoadedHolder(holderClass)) {
            return
        }

        try {
            // Load the database holder, and add it to the global collection.
            holderClass.java.newInstance()?.let { FlowManager.loadHolder(it, holderClass) }
        } catch (e: Throwable) {
            e.printStackTrace()
            throw ModuleNotFoundException("Cannot load $holderClass", e)
        }
    }

    fun loadDefaultHolderClass(className: String) {
        @Suppress("UNCHECKED_CAST")
        try {
            val defaultHolderClass = Class.forName(className) as Class<out DatabaseHolder>
            loadDatabaseHolder(defaultHolderClass.kotlin)
        } catch (e: ModuleNotFoundException) {
            // Ignore this exception since it means the application does not have its
            // own database. The initialization happens because the application is using
            // a module that has a database.
            FlowLog.log(level = W, message = e.message)
        } catch (e: ClassNotFoundException) {
            // warning if a library uses DBFlow with module support but the app you're using doesn't support it.
            FlowLog.log(level = W, message = "Could not find the default GeneratedDatabaseHolder")
        }
    }
}