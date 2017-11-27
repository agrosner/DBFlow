package com.raizlabs.android.dbflow.query.list

import android.database.Cursor

import java.io.Closeable
import java.io.IOException

/**
 * Description: Simple interface that allows you to iterate a [Cursor].
 */
interface IFlowCursorIterator<TModel> : Closeable, Iterable<TModel> {

    /**
     * @return Count of the [Cursor].
     */
    val count: Long

    /**
     * @param position The position within the [Cursor] to retrieve and convert into a [TModel]
     */
    operator fun get(position: Long): TModel

    /**
     * @return The cursor.
     */
    fun cursor(): Cursor?

    override fun iterator(): FlowCursorIterator<TModel>

    /**
     * @return Can iterate the [Cursor]. Specifies a starting location + count limit of results.
     */
    fun iterator(startingLocation: Int, limit: Long): FlowCursorIterator<TModel>

    @Throws(IOException::class)
    override fun close()
}

operator fun <TModel> IFlowCursorIterator<TModel>.get(i: Int): TModel = this[i.toLong()] ?: throw IndexOutOfBoundsException("Could not find item at index $i from the cursor.")
