package com.raizlabs.android.dbflow.test.sqlcipher

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = CipherDatabase.NAME, version = CipherDatabase.VERSION)
object CipherDatabase {

    val NAME = "CipherDatabase"
    val VERSION = 1
}
