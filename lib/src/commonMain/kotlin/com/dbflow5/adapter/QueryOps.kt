package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.config.readableTransaction
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.mpp.use
import com.dbflow5.sql.Query

interface QueryOps<QueryType : Any> {

    suspend fun DatabaseWrapper.single(query: Query): QueryType?

    suspend fun DatabaseWrapper.list(query: Query): List<QueryType>
}

@InternalDBFlowApi
data class QueryOpsImpl<QueryType : Any>(
    private val loadFromCursor: LoadFromCursor<QueryType?>
) : QueryOps<QueryType> {
    override suspend fun DatabaseWrapper.single(query: Query): QueryType? =
        generatedDatabase.readableTransaction {
            db.rawQuery(query.query).use { cursor ->
                cursor.firstOrNull()?.let { loadFromCursor(db, it) }
            }
        }

    override suspend fun DatabaseWrapper.list(query: Query): List<QueryType> =
        generatedDatabase.readableTransaction {
            db.rawQuery(query.query).use { cursor ->
                cursor.mapNotNull { loadFromCursor(db, it) }
            }
        }
}