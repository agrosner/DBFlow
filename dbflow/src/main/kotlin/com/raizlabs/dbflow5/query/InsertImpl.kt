package com.raizlabs.dbflow5.query

import android.content.ContentValues
import com.raizlabs.dbflow5.KClass
import com.raizlabs.dbflow5.query.property.Property

/**
 * Android implementation of [Insert]. Adds a [ContentValues] provided method.
 */
actual class Insert<T : Any>
internal actual constructor(table: KClass<T>, vararg columns: Property<*>) : InternalInsert<T>(table, *columns) {

    fun columnValues(contentValues: ContentValues) = apply {
        val entries = contentValues.valueSet()
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()
        for ((key) in entries) {
            columns += key
            values += contentValues.get(key)
        }

        columns(*columns.toTypedArray()).values(values)
    }
}
