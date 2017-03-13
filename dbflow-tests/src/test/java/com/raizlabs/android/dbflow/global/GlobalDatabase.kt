package com.raizlabs.android.dbflow.global

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = GlobalDatabase.NAME, version = GlobalDatabase.VERSION,
    insertConflict = ConflictAction.REPLACE, updateConflict = ConflictAction.REPLACE)
object GlobalDatabase {

    const val NAME = "GlobalDatabase"

    const val VERSION = 1
}
