package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.query.property.Property

object SQLiteJvm {

    @JvmStatic
    fun <T : Any> update(table: Class<T>) = Update(table.kotlin)

    @JvmStatic
    fun <T : Any> insert(table: Class<T>, vararg columns: Property<*>) = Insert(table.kotlin, *columns)

    @JvmStatic
    fun <T : Any> delete(table: Class<T>): From<T> = delete().from(table)
}
