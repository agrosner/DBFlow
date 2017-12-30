package com.raizlabs.dbflow5.query.list

import com.raizlabs.dbflow5.database.FlowCursor
import java.io.Closeable
import java.io.IOException

/**
 * Description: Simple interface that allows you to iterate a [FlowCursor].
 */
interface IFlowCursorIterator<TModel> : Closeable, Iterable<TModel> {

    /**
     * @return Count of the [FlowCursor].
     */
    val count: Long

    /**
     * @param index The index within the [FlowCursor] to retrieve and convert into a [TModel]
     */
    operator fun get(index: Long): TModel

    /**
     * @param index The index within the [FlowCursor] to retrieve and convert into a [TModel]
     */
    operator fun get(index: Int): TModel = get(index.toLong())

    /**
     * @return The cursor.
     */
    val cursor: FlowCursor?

    override fun iterator(): FlowCursorIterator<TModel>

    /**
     * @return Can iterate the [FlowCursor]. Specifies a starting location + count limit of results.
     */
    fun iterator(startingLocation: Int, limit: Long): FlowCursorIterator<TModel>

    @Throws(IOException::class)
    override fun close()
}