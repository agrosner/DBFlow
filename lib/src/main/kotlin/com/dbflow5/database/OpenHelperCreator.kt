package com.dbflow5.database

import com.dbflow5.config.GeneratedDatabase

fun interface OpenHelperCreator {
    fun createHelper(db: GeneratedDatabase, callback: DatabaseCallback?): OpenHelper
}

