package com.raizlabs.dbflow5.database

actual typealias Cursor = android.database.Cursor

actual val Cursor.count
    get() = this.count
