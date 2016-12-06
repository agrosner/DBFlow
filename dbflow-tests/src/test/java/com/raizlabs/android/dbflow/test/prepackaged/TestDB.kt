package com.raizlabs.android.dbflow.test.prepackaged

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(version = TestDB.VERSION, name = TestDB.NAME)
object TestDB {

    const val NAME = "testdb"

    const val VERSION = 1
}
