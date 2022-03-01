package com.dbflow5.database

import com.dbflow5.annotation.opts.InternalDBFlowApi
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A reusable lazy database property. Will create db when first accessed,
 * then if is closed will reopen when retrieved again.
 */
@InternalDBFlowApi
class DatabasePropertyDelegate<DB : DatabaseWrapper>(
    private val factory: () -> DB
) : ReadOnlyProperty<Any?, DB> {
    private var db: DB? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): DB {
        var localDB = db
        if (localDB == null || !localDB.isOpen) {
            localDB = factory()
        }
        return localDB.also { db = it }
    }
}