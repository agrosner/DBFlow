package com.raizlabs.dbflow5.database

actual typealias Cursor = android.database.Cursor

/**
 * Multi-platform trick, for some reason not picked up in compiler.
 */
@Suppress("ConflictingExtensionProperty")
actual val Cursor.count
    get() = this.count
