package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = TestDatabase.NAME, version = 3, foreignKeyConstraintsEnforced = true)
object TestDatabase {

    const val NAME = "Test"
}
