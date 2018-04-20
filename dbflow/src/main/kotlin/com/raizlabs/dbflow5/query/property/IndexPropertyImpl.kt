package com.raizlabs.dbflow5.query.property

import kotlin.reflect.KClass

/**
 * Description:
 */
actual class IndexProperty<T : Any> : InternalIndexProperty<T> {

    actual constructor(indexName: String,
                       unique: Boolean,
                       table: KClass<T>,
                       vararg properties: IProperty<*>) : super(indexName, unique, table, *properties)

    constructor(indexName: String,
                unique: Boolean,
                table: Class<T>,
                vararg properties: IProperty<*>) : super(indexName, unique, table.kotlin, *properties)
}