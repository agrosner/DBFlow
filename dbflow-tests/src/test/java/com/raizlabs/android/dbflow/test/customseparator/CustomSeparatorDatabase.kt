package com.raizlabs.android.dbflow.test.customseparator

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = CustomSeparatorDatabase.NAME, version = CustomSeparatorDatabase.VERSION,
    generatedClassSeparator = "$$")
object CustomSeparatorDatabase {

    const val NAME = "Custom"

    const val VERSION = 1
}
