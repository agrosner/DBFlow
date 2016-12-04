package com.raizlabs.android.dbflow.test.example

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = ColonyDatabase.NAME, version = ColonyDatabase.VERSION)
object ColonyDatabase {

    val NAME = "Colonies"

    val VERSION = 1
}
