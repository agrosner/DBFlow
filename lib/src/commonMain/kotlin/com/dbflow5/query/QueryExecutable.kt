package com.dbflow5.query

import com.dbflow5.database.DatabaseConnection

interface QueryExecutableIgnoreResult : ExecutableQuery<Unit> {
    override suspend fun execute(db: DatabaseConnection) =
        db.execute(this.query)
}
