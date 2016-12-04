package com.raizlabs.android.dbflow.test.customhelper

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = HelperDatabase.NAME, version = HelperDatabase.VERSION)
object HelperDatabase {

    const val NAME = "Helper"

    const val VERSION = 1
}
