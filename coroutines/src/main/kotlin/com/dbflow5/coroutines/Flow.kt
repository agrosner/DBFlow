package com.dbflow5.coroutines

import com.dbflow5.config.databaseForTable
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.ModelQueriableEvalFn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Constructs a single-use [Flow] for use in coroutines.
 */
fun <T : Any, R : Any?> ModelQueriable<T>.asFlow(
    evalFn: ModelQueriableEvalFn<T, R>
): Flow<R> =
    flow {
        val result = databaseForTable(table.kotlin).executeTransaction { db ->
            evalFn(db)
        }
        emit(result)
    }
