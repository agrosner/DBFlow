package com.raizlabs.dbflow5.adapter

import com.raizlabs.dbflow5.database.DBFlowDatabase
import kotlin.reflect.KClass

/**
 * Description:
 */
actual abstract class RetrievalAdapter<T : Any>
actual constructor(databaseDefinition: DBFlowDatabase) : InternalRetrievalAdapter<T>(databaseDefinition) {

    abstract val table: Class<T>

    override val kTable: KClass<T>
        get() = table.kotlin
}