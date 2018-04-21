package com.raizlabs.dbflow5.query

import kotlin.reflect.KClass
import com.raizlabs.dbflow5.sql.Query

actual class Set<T : Any> internal actual constructor(queryBuilderBase: Query, table: KClass<T>)
    : InternalSet<T>(queryBuilderBase, table)