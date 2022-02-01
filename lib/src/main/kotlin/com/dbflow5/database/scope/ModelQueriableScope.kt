package com.dbflow5.database.scope

import com.dbflow5.query.ModelQueriable
import kotlin.reflect.KClass

/**
 * Description:
 */
interface ModelQueriableScope {
    suspend fun <T : Any> ModelQueriable<T>.queryList(): List<T>

    suspend fun <T : Any> ModelQueriable<T>.querySingle(): T?

    suspend fun <T : Any> ModelQueriable<T>.requireSingle(): T

    suspend fun <T : Any, R : Any> ModelQueriable<T>.customList(queryModelClass: KClass<R>): List<R>

    suspend fun <T : Any, R : Any> ModelQueriable<T>.customSingle(queryModelClass: KClass<R>): R?

    suspend fun <T : Any, R : Any> ModelQueriable<T>.requireCustomSingle(queryModelClass: KClass<R>): R
}
