package com.dbflow5.reactivestreams.structure

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.database.DatabaseWrapper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlin.reflect.KClass

/**
 * Description: Mirrors the [RetrievalAdapter] with subset of exposed methods, mostly for
 * [.load] and [.exists]
 */
open class RXRetrievalAdapter<T : Any>
internal constructor(private val retrievalAdapter: RetrievalAdapter<T>) {

    internal constructor(table: KClass<T>) : this(FlowManager.getRetrievalAdapter<T>(table))

    fun load(model: T, databaseWrapper: DatabaseWrapper): Completable = Completable.fromCallable {
        retrievalAdapter.load(model, databaseWrapper)
        null
    }

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    fun exists(model: T, wrapper: DatabaseWrapper): Single<Boolean> =
        Single.fromCallable { retrievalAdapter.exists(model, wrapper) }

    companion object {

        @JvmStatic
        fun <T : Any> from(modelAdapter: RetrievalAdapter<T>): RXRetrievalAdapter<T> =
            RXRetrievalAdapter(modelAdapter)

        @JvmStatic
        fun <T : Any> from(table: KClass<T>): RXRetrievalAdapter<T> = RXRetrievalAdapter(table)
    }
}
