package com.raizlabs.dbflow5.database

actual typealias Cursor = android.database.Cursor

/**
 * Multi-platform trick, for some reason not picked up in compiler.
 */
@Suppress("ConflictingExtensionProperty")
actual val Cursor.count
    get() = this.count

/**
 * Implements the Android way of reading a cursor. Check null first, then read value if not null.
 */
actual inline fun <T> Cursor.getValue(index: Int, defaultValue: T, getter: () -> T): T {
    return if (index != -1 && !isNull(index)) {
        getter()
    } else {
        defaultValue
    }
}