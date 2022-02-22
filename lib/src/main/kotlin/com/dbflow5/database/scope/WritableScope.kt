package com.dbflow5.database.scope

import com.dbflow5.adapter2.ModelAdapter

/**
 * Description:
 */
interface WritableScope {

    suspend fun <T : Any> ModelAdapter<T>.save(model: T): T
    suspend fun <T : Any> ModelAdapter<T>.saveAll(models: Collection<T>): Collection<T>
    suspend fun <T : Any> ModelAdapter<T>.insert(model: T): T
    suspend fun <T : Any> ModelAdapter<T>.insertAll(models: Collection<T>): Collection<T>
    suspend fun <T : Any> ModelAdapter<T>.update(model: T): T
    suspend fun <T : Any> ModelAdapter<T>.updateAll(models: Collection<T>): Collection<T>
    suspend fun <T : Any> ModelAdapter<T>.delete(model: T): T
    suspend fun <T : Any> ModelAdapter<T>.deleteAll(models: Collection<T>): Collection<T>
}
