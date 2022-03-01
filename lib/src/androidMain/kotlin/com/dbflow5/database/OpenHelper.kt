package com.dbflow5.database

import com.dbflow5.config.GeneratedDatabase

/**
 * Android specific default open helper.
 */
actual fun OpenHelper(db: GeneratedDatabase, callback: DatabaseCallback?): OpenHelper =
    AndroidSQLiteOpenHelper(db.platformSettings.context, db, callback)
