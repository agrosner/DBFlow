package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = KotlinTestDatabase.NAME, version = KotlinTestDatabase.VERSION)
object KotlinTestDatabase {

    val NAME = "KotlinTest"

    val VERSION = 1
}
