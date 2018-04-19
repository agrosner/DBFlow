package com.raizlabs.dbflow5.config

import kotlin.reflect.KClass

/**
 * Description:
 */
actual abstract class DBFlowDatabase : InternalDBFlowDatabase() {

    abstract val associatedDatabaseClassFile: Class<*>

    override val associatedDatabaseKClassFile: KClass<*>
        get() = associatedDatabaseClassFile.kotlin
}