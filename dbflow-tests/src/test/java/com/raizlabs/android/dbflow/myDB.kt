package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = myDB.NAME, version = myDB.VERSION)
object myDB {
    const val NAME = "test"
    const val VERSION = 1
}
