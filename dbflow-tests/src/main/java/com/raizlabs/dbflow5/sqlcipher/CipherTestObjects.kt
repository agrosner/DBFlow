package com.raizlabs.dbflow5.sqlcipher

import android.content.Context
import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.database.DatabaseCallback
import com.raizlabs.dbflow5.structure.BaseModel

class SQLCipherOpenHelperImpl(context: Context,
                              databaseDefinition: DBFlowDatabase,
                              callback: DatabaseCallback?)
    : SQLCipherOpenHelper(context, databaseDefinition, callback) {
    override val cipherSecret get() = "dbflow-rules"
}

@Table(database = CipherDatabase::class)
class CipherModel(@PrimaryKey(autoincrement = true) var id: Long = 0,
                  @Column var name: String? = null) : BaseModel()