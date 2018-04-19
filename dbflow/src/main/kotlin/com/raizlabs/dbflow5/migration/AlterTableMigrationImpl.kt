package com.raizlabs.dbflow5.migration

import kotlin.reflect.KClass

/**
 * Description: JVM specific implementation with [Class] use.
 */
actual open class AlterTableMigration<T : Any>(table: KClass<T>) : InternalAlterTableMigration<T>(table) {

    constructor(table: Class<T>) : this(table.kotlin)
}