package com.dbflow5.delegates

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.database.DatabaseConnection
import kotlin.properties.ReadOnlyProperty

@InternalDBFlowApi
fun <DB : DatabaseConnection> databaseProperty(
    factory: () -> DB
): ReadOnlyProperty<Any?, DB> = CheckOpenPropertyDelegate(factory)
