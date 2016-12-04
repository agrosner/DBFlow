package com.raizlabs.android.dbflow.test.sqlcipher

import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.dbflow.android.sqlcipher.SQLCipherOpenHelper

/**
 * Description: Simple implementation.
 */
class SQLCipherHelperImpl(databaseDefinition: DatabaseDefinition, listener: DatabaseHelperListener) : SQLCipherOpenHelper(databaseDefinition, listener) {

    override fun getCipherSecret(): String {
        return "dbflow-rules"
    }
}
