package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.query.property.IProperty

/**
 * Description:
 */
actual open class Select
actual constructor(vararg properties: IProperty<*>) : InternalSelect(*properties) {

    fun <T : Any> from(table: Class<T>): From<T> = from(table.kotlin)
}