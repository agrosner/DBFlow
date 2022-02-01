package com.dbflow5.database.scope

import com.dbflow5.adapter.ModelAdapter

/**
 * Description:
 */
interface WritableScope {

    suspend fun <T : Any> ModelAdapter<T>.save(model: T): Result<T>
    suspend fun <T : Any> ModelAdapter<T>.saveAll(models: Collection<T>): Result<Collection<T>>
    suspend fun <T : Any> ModelAdapter<T>.insert(model: T): Result<T>
    suspend fun <T : Any> ModelAdapter<T>.insertAll(models: Collection<T>): Result<Collection<T>>
    suspend fun <T : Any> ModelAdapter<T>.update(model: T): Result<T>
    suspend fun <T : Any> ModelAdapter<T>.updateAll(models: Collection<T>): Result<Collection<T>>
    suspend fun <T : Any> ModelAdapter<T>.delete(model: T): Result<T>
    suspend fun <T : Any> ModelAdapter<T>.deleteAll(models: Collection<T>): Result<Collection<T>>
    suspend fun <T : Any> ModelAdapter<T>.exists(model: T): Boolean
    suspend fun <T : Any> ModelAdapter<T>.load(model: T): T?
}
