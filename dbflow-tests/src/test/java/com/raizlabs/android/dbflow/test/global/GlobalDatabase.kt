package com.raizlabs.android.dbflow.test.global

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = GlobalDatabase.NAME, version = GlobalDatabase.VERSION, insertConflict = ConflictAction.REPLACE, updateConflict = ConflictAction.REPLACE)
object GlobalDatabase {

    val NAME = "GlobalDatabase"

    val VERSION = 1
}
