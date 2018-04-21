package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.query.property.Property
import kotlin.reflect.KClass

actual class Insert<T : Any>
internal actual constructor(table: KClass<T>, vararg columns: Property<*>) : InternalInsert<T>(table, *columns)