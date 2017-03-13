package com.raizlabs.android.dbflow.example

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = ColonyDatabase.NAME, version = ColonyDatabase.VERSION)
object ColonyDatabase {

    const val NAME = "Colonies"

    const val VERSION = 1
}
