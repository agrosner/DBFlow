package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description: Shows example for support for Kotlin
 */
@Database(version = KotlinDatabase.VERSION, name = KotlinDatabase.NAME, generatedClassSeparator = "_")
object KotlinDatabase {

    val NAME = "KotlinDatabase"

    val VERSION = 1
}
