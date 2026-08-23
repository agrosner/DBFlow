package com.dbflow5.database

fun interface OpenHelperCreator {
    fun createHelper(db: GeneratedDatabase, callback: DatabaseCallback?): OpenHelper
}

