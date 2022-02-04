package com.dbflow5.reactivestreams.structure

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.database.DatabaseWrapper
import io.reactivex.rxjava3.core.Single

/**
 * Description: Mirrors the [RetrievalAdapter] with subset of exposed methods, mostly for
 * [.load] and [.exists]
 */
open class RXRetrievalAdapter<T : Any>
internal constructor(private val retrievalAdapter: SQLObjectAdapter<T>) {

    fun load(model: T, databaseWrapper: DatabaseWrapper): Single<T> = Single.fromCallable {
        retrievalAdapter.loadSingle(model, databaseWrapper)
    }

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    fun exists(model: T, wrapper: DatabaseWrapper): Single<Boolean> =
        Single.fromCallable { retrievalAdapter.exists(model, wrapper) }

    companion object {

        @JvmStatic
        fun <T : Any> from(modelAdapter: SQLObjectAdapter<T>): RXRetrievalAdapter<T> =
            RXRetrievalAdapter(modelAdapter)
    }
}
