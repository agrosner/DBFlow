package com.dbflow5.reactivestreams.structure

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.database.DatabaseWrapper
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.rx3.rxMaybe
import kotlinx.coroutines.rx3.rxSingle

/**
 * Description: Mirrors the [RetrievalAdapter] with subset of exposed methods, mostly for
 * [.load] and [.exists]
 */
open class RXRetrievalAdapter<T : Any>
internal constructor(private val retrievalAdapter: RetrievalAdapter<T>) {

    internal constructor(table: Class<T>) : this(FlowManager.getRetrievalAdapter<T>(table))

    fun load(model: T, databaseWrapper: DatabaseWrapper): Maybe<T> = rxMaybe {
        retrievalAdapter.load(model, databaseWrapper)
    }

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    fun exists(model: T, wrapper: DatabaseWrapper): Single<Boolean> =
        rxSingle { retrievalAdapter.exists(model, wrapper) }

    companion object {

        @JvmStatic
        fun <T : Any> from(modelAdapter: RetrievalAdapter<T>): RXRetrievalAdapter<T> =
            RXRetrievalAdapter(modelAdapter)

        @JvmStatic
        fun <T : Any> from(table: Class<T>): RXRetrievalAdapter<T> = RXRetrievalAdapter(table)
    }
}
