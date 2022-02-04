package com.dbflow5.database.scope

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.query.ModelQueriable

/**
 * Description:
 */
interface ModelQueriableScope {
    suspend fun <T : Any> ModelQueriable<T>.queryList(): List<T>

    suspend fun <T : Any> ModelQueriable<T>.querySingle(): T?

    suspend fun <T : Any> ModelQueriable<T>.requireSingle(): T

    suspend fun <T : Any, R : Any> ModelQueriable<T>.customList(retrievalAdapter: RetrievalAdapter<R>): List<R>

    suspend fun <T : Any, R : Any> ModelQueriable<T>.customSingle(retrievalAdapter: RetrievalAdapter<R>): R?

    suspend fun <T : Any, R : Any> ModelQueriable<T>.requireCustomSingle(retrievalAdapter: RetrievalAdapter<R>): R
}
