package com.raizlabs.dbflow5.sqlcipher

import com.raizlabs.dbflow5.annotation.Database

@Database(version = CipherDatabase.VERSION)
object CipherDatabase {

    const val VERSION = 1
}