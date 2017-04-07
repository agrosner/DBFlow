package com.raizlabs.android.dbflow.sqlcipher

import com.raizlabs.android.dbflow.annotation.Database

@Database(name = CipherDatabase.NAME, version = CipherDatabase.VERSION)
object CipherDatabase {

    const val NAME = "CipherDatabase"
    const val VERSION = 1
}