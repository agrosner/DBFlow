package com.dbflow5.delegates

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.database.DatabaseWrapper
import kotlin.properties.ReadOnlyProperty

@InternalDBFlowApi
fun <DB : DatabaseWrapper> databaseProperty(
    factory: () -> DB
): ReadOnlyProperty<Any?, DB> = CheckOpenPropertyDelegate(factory)
