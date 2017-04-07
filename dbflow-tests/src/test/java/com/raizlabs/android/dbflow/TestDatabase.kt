package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(version = TestDatabase.VERSION, name = TestDatabase.NAME)
object TestDatabase {

    const val VERSION = 1

    const val NAME = "TestDatabase";

}