package com.raizlabs.dbflow5.database

import kotlin.reflect.KClass

actual abstract class DBFlowDatabase : InternalDBFlowDatabase() {

    abstract val associatedDatabaseClassFile: Class<*>

    override val associatedDatabaseKClassFile: KClass<*>
        get() = associatedDatabaseClassFile.kotlin
}