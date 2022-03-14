package com.dbflow5.test

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.annotation.Database
import com.dbflow5.database.DBFlowDatabase

@Database(
    version = 1,
    tables = [
        CipherModel::class,
    ]
)
abstract class CipherDatabase : DBFlowDatabase<CipherDatabase>() {

    abstract val cipherAdapter: ModelAdapter<CipherModel>
}
