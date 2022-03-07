package com.dbflow5.query

import com.dbflow5.database.DatabaseWrapper

interface QueryExecutableIgnoreResult : ExecutableQuery<Unit> {
    override suspend fun execute(db: DatabaseWrapper) =
        db.execSQL(this.query)
}
