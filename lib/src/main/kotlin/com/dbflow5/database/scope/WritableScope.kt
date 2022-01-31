package com.dbflow5.database.scope

import com.dbflow5.adapter.ModelAdapter

/**
 * Description:
 */
interface WritableScope {

    fun <T : Any> ModelAdapter<T>.save(model: T): Result<T>

    fun <T : Any> ModelAdapter<T>.insert(model: T): Result<T>

    fun <T : Any> ModelAdapter<T>.update(model: T): Result<T>

    fun <T : Any> ModelAdapter<T>.delete(model: T): Result<T>

    fun <T : Any> ModelAdapter<T>.exists(model: T): Boolean

    fun <T : Any> ModelAdapter<T>.load(model: T): T?
}
