package com.dbflow5.query.list

import com.dbflow5.database.FlowCursor
import java.io.Closeable
import java.io.IOException

/**
 * Description: Simple interface that allows you to iterate a [FlowCursor].
 */
interface IFlowCursorIterator<T : Any> : Closeable, Iterable<T> {

    /**
     * @return Count of the [FlowCursor].
     */
    val count: Long

    /**
     * @param index The index within the [FlowCursor] to retrieve and convert into a [T]
     */
    operator fun get(index: Long): T

    /**
     * @param index The index within the [FlowCursor] to retrieve and convert into a [T]
     */
    operator fun get(index: Int): T = get(index.toLong())

    /**
     * @return The cursor.
     */
    val cursor: FlowCursor?

    /**
     * If true, we are tracking a passed cursor. If not, we are using new cursor constructed within this class.
     */
    val trackingCursor: Boolean

    /**
     * If true, [FlowCursor] is closed and this object should be discarded.
     */
    val isClosed: Boolean

    override fun iterator(): FlowCursorIterator<T>

    /**
     * @return Can iterate the [FlowCursor]. Specifies a starting location + count limit of results.
     */
    fun iterator(startingLocation: Long, limit: Long): FlowCursorIterator<T>

    @Throws(IOException::class)
    override fun close()
}