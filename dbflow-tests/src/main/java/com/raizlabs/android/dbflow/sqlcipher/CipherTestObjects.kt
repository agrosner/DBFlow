package com.raizlabs.android.dbflow.sqlcipher

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener

class SQLCipherOpenHelperImpl(databaseDefinition: DatabaseDefinition, helperListener: DatabaseHelperListener?)
    : SQLCipherOpenHelper(databaseDefinition, helperListener) {
    override fun getCipherSecret() = "dbflow-rules"
}

@Table(database = CipherDatabase::class)
class CipherModel(@PrimaryKey(autoincrement = true) var id: Long = 0,
                  @Column var name: String? = null) : BaseModel()