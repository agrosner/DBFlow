package com.raizlabs.android.dbflow.kotlin

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = KotlinTestDatabase.NAME, version = KotlinTestDatabase.VERSION)
object KotlinTestDatabase {

    const val NAME = "KotlinTest"

    const val VERSION = 1
}
